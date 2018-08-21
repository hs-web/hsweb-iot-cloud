package org.hswebframework.iot.interaction.vertx.client;

import com.alibaba.fastjson.JSON;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.iot.interaction.core.ErrorCode;
import org.hswebframework.iot.interaction.core.IotCommand;
import org.hswebframework.iot.interaction.core.IotCommandSender;
import org.hswebframework.iot.interaction.events.CommandSendEvent;
import org.hswebframework.iot.interaction.events.DeviceConnectEvent;
import org.hswebframework.iot.interaction.events.DeviceDisconnectEvent;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.concurrent.counter.Counter;
import org.hswebframework.web.concurrent.counter.CounterManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Component
@Slf4j
public class HashMapClientRepository implements ClientRepository, IotCommandSender {

    @Autowired
    private Vertx vertx;

    private Map<String, Client> repository = new ConcurrentHashMap<>();

    @Autowired
    private CounterManager counterManager;

    private Map<String, MessageConsumer<?>> consumerMap = new ConcurrentHashMap<>();

    private long timeout = 10 * 60 * 1000L;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public Client getClient(String idOrClientId) {
        Client client = repository.get(idOrClientId);

        if (client != null) {
            //客户端太久没有ping
            if (System.currentTimeMillis() - client.lastPingTime() > timeout) {
                log.debug("客户端[{}]链接失效,上一次ping时间:{} ", idOrClientId, client.lastPingTime());
                unregister(client.getClientId());
                return null;
            }
        }
        return client;
    }

    protected Client doRegister(Client client) {
        log.debug("注册客户端:{}", client);
        repository.put(client.getClientId(), client);
        //如果id和clientId不同,则同时注册
        if (!client.getClientId().equals(client.getId())) {
            repository.put(client.getId(), client);
        }

        //发布设备连接给spring事件
        eventPublisher.publishEvent(new DeviceConnectEvent(client.getClientId(), new Date()));

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
        counter.add(1L);
        return client;
    }

    @Override
    public Client register(Client client) {
        Client old = getClient(client.getClientId());
        if (null != old) {
            doUnregister(old.getClientId(), (success) -> {
                doRegister(client);
            });
        } else {
            doRegister(client);
        }

        return old;
    }

    private Counter counter;

    @PostConstruct
    public void init() {
        counter = counterManager.getCounter("iot-device-client-counter");
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

    public Client doUnregister(String idOrClientId, Consumer<Boolean> supplier) {
        Client old = repository.remove(idOrClientId);
        if (old != null) {
            log.debug("注销客户端:{}", old);
            repository.remove(old.getId());
            repository.remove(old.getClientId());
            if (vertx.isClustered()) {
                Optional.ofNullable(consumerMap.get(old.getClientId()))
                        .ifPresent(consumer -> consumer.unregister(result -> {
                            supplier.accept(result.succeeded());
                            if (result.succeeded()) {
                                log.debug("unregister event bus consumer:[{}] success ", old.getClientId());
                            } else {
                                log.error("unregister event bus consumer:[{}] error ", old.getClientId(), result.cause());
                            }
                        }));
            } else {
                supplier.accept(true);
            }
            //发布连接断开事件给spring
            eventPublisher.publishEvent(new DeviceDisconnectEvent(old.getClientId(), new Date()));
            old.close();
            counter.getAndAdd(-1L);
        } else {
            supplier.accept(false);
        }
        return old;
    }

    @Override
    public Client unregister(String idOrClientId) {
        return doUnregister(idOrClientId, (success) -> {
        });
    }

    protected String createCommandSendEventAddress(String clientId) {
        return "iot-command-".concat(clientId);
    }

    @Override
    @SneakyThrows
    public void send(String topic, String clientId, IotCommand command) {
        command.tryValidate();
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
    }
}
