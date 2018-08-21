package org.hswebframework.iot.interaction.vertx.mqtt;

import com.alibaba.fastjson.JSON;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttEndpoint;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttServerOptions;
import io.vertx.mqtt.MqttTopicSubscription;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.iot.interaction.authority.DeviceAuthorityService;
import org.hswebframework.iot.interaction.core.Topics;
import org.hswebframework.iot.interaction.events.CommandReplyEvent;
import org.hswebframework.iot.interaction.events.DeviceReportEvent;
import org.hswebframework.iot.interaction.vertx.client.Client;
import org.hswebframework.iot.interaction.vertx.client.ClientRepository;
import org.hswebframework.iot.interaction.vertx.client.message.ClientMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhouhao
 * @see MqttServerVerticleSupplier
 * @since 1.0.0
 */
@Slf4j
//@Component //实例存在多个,不交给spring管理
public class VertxMqttServer extends AbstractVerticle {

    @Autowired
    private ClientRepository clientRepository;

    @Value("${vertx.service-id}")
    private String serviceId;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private MqttServerOptions mqttServerOptions;

    @Autowired
    private DeviceAuthorityService authorityService;

    @Override
    public void start() throws Exception {
        MqttServer mqttServer = MqttServer.create(vertx, mqttServerOptions);
        mqttServer.endpointHandler(mqttEndpoint -> {
            String clientId = mqttEndpoint.clientIdentifier();
            log.debug("接收到MQTT客户端[{}]消息", clientId);
            //执行创建链接
            doConnect(mqttEndpoint);

        }).listen(result -> {
            if (result.succeeded()) {
                int port = mqttServer.actualPort();
                log.debug("MQTT server started on port {}", port);
            } else {
                log.warn("MQTT server start failed", result.cause());
            }
        });
    }

    protected void doConnect(MqttEndpoint endpoint) {
        if (endpoint.auth() == null) {
            endpoint.reject(MqttConnectReturnCode.CONNECTION_REFUSED_NOT_AUTHORIZED);
            return;
        }
        String userName = endpoint.auth().userName();
        String passWord = endpoint.auth().password();

        if (authorityService.verification(endpoint.clientIdentifier(), userName, passWord)) {
            log.debug("MQTT客户端:{}认证通过", endpoint.clientIdentifier());
            acceptConnect(endpoint);
        } else {
            log.warn("客户端[{}]用户名密码错误", endpoint.clientIdentifier());
            endpoint.reject(MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD);
        }
    }

    protected void acceptConnect(MqttEndpoint endpoint) {
        String clientId = endpoint.clientIdentifier();
        MqttClient client = new MqttClient(endpoint);

        endpoint.accept(false)
                .closeHandler(v -> {
                    log.debug("[{}] closed", clientId);
                    Client old = clientRepository.getClient(clientId);
                    if (old == client) {
                        clientRepository.unregister(clientId);
                    } else {
                        log.debug("client {} is unregistered", client);
                    }
                })
                .subscribeHandler(subscribe -> {
                    List<MqttQoS> grantedQosLevels = new ArrayList<>();
                    for (MqttTopicSubscription s : subscribe.topicSubscriptions()) {
                        log.info("[{}] Subscription for {} with QoS {}", clientId, s.topicName(), s.qualityOfService());
                        grantedQosLevels.add(s.qualityOfService());
                    }
                    // ack the subscriptions request
                    endpoint.subscribeAcknowledge(subscribe.messageId(), grantedQosLevels);

                    // specifing handlers for handling QoS 1 and 2
                    endpoint.publishAcknowledgeHandler(messageId -> log.info("[{}] Received ack for message = {}", clientId, messageId))
                            .publishReceivedHandler(endpoint::publishRelease)
                            .publishCompletionHandler(messageId -> log.info("[{}] Received ack for message = {}", clientId, messageId));
                })
                .unsubscribeHandler(unsubscribe -> {
                    for (String t : unsubscribe.topics()) {
                        log.info("[{}] Unsubscription for {}", clientId, t);
                    }
                    // ack the subscriptions request
                    endpoint.unsubscribeAcknowledge(unsubscribe.messageId());
                })
                .disconnectHandler(v -> log.info("[{}] Received disconnect from client", clientId))
                .exceptionHandler(e -> log.error(clientId, e))
                .publishHandler(message -> {
                    //设备推送了消息
                    String topicName = message.topicName();
                    Buffer buffer = message.payload();
                    String payload = buffer.toString();
                    log.info("接受到客户端消息推送:[{}] payload [{}] with QoS [{}]", topicName, payload, message.qosLevel());
                    if (message.qosLevel() == MqttQoS.AT_LEAST_ONCE) {
                        endpoint.publishAcknowledge(message.messageId());
                    } else if (message.qosLevel() == MqttQoS.EXACTLY_ONCE) {
                        endpoint.publishReceived(message.messageId());
                    }
                    try {
                        ClientMessage event = null;
                        //目前仅支持reply和report的topic
                        if (Topics.reply.equals(topicName)) {
                            event = JSON.parseObject(payload, CommandReplyEvent.class);
                        } else if (Topics.report.equals(topicName)) {
                            event = JSON.parseObject(payload, DeviceReportEvent.class);
                        }
                        if (null != event) {
                            event.setClientId(clientId);
                            //发布事件到spring
                            eventPublisher.publishEvent(event);
                        } else {
                            log.warn("不支持的topic:{} => {}", topicName, payload);
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                })
                .publishReleaseHandler(messageId -> {
                    log.debug("complete message :{}", messageId);
                    endpoint.publishComplete(messageId);
                });
        //注册设备
        clientRepository.register(client);
    }

}
