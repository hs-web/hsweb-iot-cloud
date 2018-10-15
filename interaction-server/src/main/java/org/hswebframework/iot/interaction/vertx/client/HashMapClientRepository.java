package org.hswebframework.iot.interaction.vertx.client;

import com.alibaba.fastjson.JSON;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.impl.NoStackTraceThrowable;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.iot.interaction.core.ErrorCode;
import org.hswebframework.iot.interaction.core.IotCommand;
import org.hswebframework.iot.interaction.core.IotCommandSender;
import org.hswebframework.iot.interaction.events.CommandSendEvent;
import org.hswebframework.iot.interaction.events.DeviceConnectEvent;
import org.hswebframework.iot.interaction.events.DeviceDisconnectEvent;
import org.hswebframework.iot.logging.RedissonClientRepository;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.concurrent.counter.Counter;
import org.hswebframework.web.concurrent.counter.CounterManager;
import org.redisson.api.RBucket;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * @author zhouhao
 * @since 1.1.0
 */
@Component
@Slf4j(topic = "business.vertx.mqtt.client.repo")
public class HashMapClientRepository implements ClientRepository, IotCommandSender {

    @Value("${vertx.no-cluster:false}")
    private boolean noCluster = false;

    @Value("${vertx.cluster-host:localhost}")
    private String clusterHost = "localhost";

    @Autowired
    private Vertx vertx;

    private Map<String, Client> repository = new ConcurrentHashMap<>();

    @Autowired
    private CounterManager counterManager;

    @Autowired
    private RedissonClientRepository clientRepository;

    private Map<String, MessageConsumer<?>> consumerMap = new ConcurrentHashMap<>();

    private long timeout = 10 * 60 * 1000L;

    @Value("${vertx.mqtt.client.check-alive:20000}")
    private long checkAlivePeriodic = 20000;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public Client getClient(String idOrClientId) {
        Client client = repository.get(idOrClientId);

        if (client != null && clientIsAlive(client)) {
            return client;
        }
        return null;
    }

    private void putNoClusterClient(String clientId) {
        getNoClusterClientBucket(clientId)
                .set(clusterHost);
    }

    private RBucket<String> getNoClusterClientBucket(String clientId) {
        return clientRepository.getClient("deviceRedisRegisterInfo")
                .orElseGet(clientRepository::getDefaultClient)
                .getBucket(clientId, StringCodec.INSTANCE);
    }

    private boolean clientIsAlive(Client client) {
        if (System.currentTimeMillis() - client.lastPingTime() > timeout || !client.alive()) {
            log.debug("客户端[{}]连接失效", client.getClientId());
            unregister(client.getClientId());
            return false;
        }
        return true;
    }

    protected Client doRegister(Client client) {
        log.debug("注册客户端:{}", client);
        repository.put(client.getClientId(), client);
        //如果id和clientId不同,则同时注册
        if (!client.getClientId().equals(client.getId())) {
            repository.put(client.getId(), client);
        }

        //发布设备连接给spring事件
        eventPublisher.publishEvent(new DeviceConnectEvent(client.getClientId(), client.connectTime()));
        if (vertx.isClustered()) {
            //集群下通过eventBus来接收从集群的其他节点发来的命令
            /*
                场景:
                1. 集群中有2个节点A,B
                2. 客户端[client_0001]注册到了节点A,节点A会在eventBus订阅事件,地址[iot-command-client_0001]
                3. 节点B接收到了来自其他服务(plugin-server)向客户端[client_0001]发送指令的请求
                4. 节点B发现客户端[client_0001]并没有在节点B中注册
                5. 节点B往eventBus中的地址[iot-command-client_0001]发送CommandSendEvent事件
                6. 节点A接收到事件,向注册到本地的客户端[client_0001]发送命令,并回复发送情况
             */
            MessageConsumer consumer = vertx.eventBus()
                    .consumer(createCommandSendEventAddress(client.getClientId()), msg -> {
                        Object event = msg.body();
                        try {
                            log.info("接收到从eventBus发送的命令:{}", event);
                            if (event instanceof String) {
                                event = JSON.parseObject(((String) event), CommandSendEvent.class);
                            }
                            if (event instanceof CommandSendEvent) {
                                CommandSendEvent e = ((CommandSendEvent) event);
                                boolean success = doLocalSend(e.getTopic(), e.getClientId(), e.getCommand());
                                if (success) {
                                    msg.reply("ok");
                                } else {
                                    msg.reply("客户端未注册");
                                }
                            }
                        } catch (Exception e) {
                            log.warn(e.getMessage(), e);
                            msg.reply("error:" + e.getMessage());
                        }
                    });
            consumerMap.put(client.getClientId(), consumer);

        }
        if (noCluster) {
            putNoClusterClient(client.getClientId());
        }
        counter.increment();
        return client;
    }

    @Override
    public Client register(Client client) {
        Client old = getClient(client.getClientId());
        if (null != old) {
            doUnregister(old.getClientId(), (success) -> {
                doRegister(client);
            }, false);
        } else {
            doRegister(client);
        }

        return old;
    }

    private Counter counter;

    @PostConstruct
    public void init() {
        counter = counterManager.getCounter("iot-device-client-counter");
        vertx.setPeriodic(checkAlivePeriodic, id -> {
            List<Client> tmp = new ArrayList<>(repository.values());
            try {
                long total = tmp.size();
                long alive = tmp.stream().filter(this::clientIsAlive).count();
                log.debug("检查MQTT连接状态,当前节点客户端:{}/{},集群中客户端数量:{}"
                        , alive
                        , total
                        , counter.get());
            } catch (Exception e) {
                log.error("检查MQTT连接状态失败", e);
            }
        });
    }

    @Override
    public long total() {
        return counter.get();
    }

    protected boolean doLocalSend(String topic, String clientId, IotCommand command) {
        Client client = getClient(clientId);
        if (null != client) {
            log.debug("向客户端[{}]发送指令:{}", clientId, command);
            //注册在本地则直接推送
            client.send(topic, command);
            return true;
        } else {
            log.warn("设备[{}]未注册,发送命令失败:[{}]", clientId, command);
            return false;
        }
    }

    public Client doUnregister(String idOrClientId, Consumer<Boolean> supplier, boolean pushEvent) {
        Client old = repository.remove(idOrClientId);
        if (old != null) {
            log.debug("注销客户端:{}", old);
            repository.remove(old.getId());
            repository.remove(old.getClientId());
            //发布连接断开事件给spring
            if (pushEvent) {
                eventPublisher.publishEvent(new DeviceDisconnectEvent(old.getClientId(), old.connectTime() + 1000));
            }
            if (vertx.isClustered()) {
                Optional.ofNullable(consumerMap.get(old.getClientId()))
                        .ifPresent(consumer -> consumer.unregister(result -> {
                            supplier.accept(result.succeeded());
                            if (!result.succeeded()) {
                                if (result.cause() instanceof NoStackTraceThrowable) {
                                    return;
                                }
                                log.error("unregister event bus consumer:[{}] error ", old.getClientId(), result.cause());
                            }
                        }));
            } else {
                supplier.accept(true);
            }
            if (noCluster) {
                getNoClusterClientBucket(old.getClientId()).delete();
            }
            if (counter.decrementAndGet() <= 0) {
                counter.set(0);
            }
            old.close();
        } else {
            supplier.accept(false);
        }
        return old;
    }

    @Override
    public Client unregister(String idOrClientId) {
        return doUnregister(idOrClientId, (success) -> {
        }, true);
    }

    protected String createCommandSendEventAddress(String clientId) {
        return "iot-command-".concat(clientId);
    }

    @Override
    @SneakyThrows
    public void send(String topic, String clientId, IotCommand command) {
        command.tryValidate();
        try {
            Client client = getClient(clientId);
            if (null != client) {
                doLocalSend(topic, clientId, command);
            } else if (vertx.isClustered()) {
                //集群下使用eventBus发送事件
                String address = createCommandSendEventAddress(clientId);
                log.debug("尝试向集群中的其他节点发送客户端[{}]命令:{}", clientId, command);
                CountDownLatch latch = new CountDownLatch(1);
                AtomicReference<Throwable> errorReference = new AtomicReference<>();
                vertx.eventBus()
                        .send(address, CommandSendEvent.builder()
                                        .topic(topic)
                                        .clientId(clientId)
                                        .command(command)
                                        .build()
                                        .toJSONString(),
                                handler -> {
                                    try {
                                        if (handler.succeeded()) {
                                            log.debug("成功向集群中的其他节点发送客户端[{}]命令:{},节点返回:{}", clientId, command, handler.result().body());

                                            Object body = handler.result().body();
                                            if (!"ok".equals(body)) {
                                                errorReference.set(new BusinessException("设备未注册", ErrorCode.unregistered.getValue()));
                                            }
                                        } else {
                                            errorReference.set(handler.cause());
                                        }
                                    } finally {
                                        latch.countDown();
                                    }
                                });
                try {
                    boolean success = latch.await(10, TimeUnit.SECONDS);
                    if (!success) {
                        throw new BusinessException("等待执行超时", ErrorCode.timeout.getValue());
                    }
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                    return;
                }
                //判断错误原因
                Throwable error = errorReference.get();
                if (error != null) {
                    if (error instanceof ReplyException) {
                        ReplyFailure failure = ((ReplyException) error).failureType();
                        switch (failure) {
                            case TIMEOUT:
                                throw new BusinessException("等待执行超时", ErrorCode.timeout.getValue());
                            case NO_HANDLERS:
                                throw new BusinessException("设备未注册", ErrorCode.unregistered.getValue());
                            default:
                                throw new BusinessException("发送指令失败:" + failure, ErrorCode.unknown.getValue());
                        }
                    }
                    throw error;
                }
            } else {
                throw new BusinessException("设备未注册", ReplyFailure.NO_HANDLERS.name());
            }
        } catch (BusinessException e) {
            //抛出了设备未注册异常,可能设备在线信息与平台不一致,推送设备下线事件
            if (ErrorCode.unregistered.getValue().equals(e.getCode())) {
                eventPublisher.publishEvent(new DeviceDisconnectEvent(clientId, System.currentTimeMillis()));
            }
            throw e;
        }
    }
}
