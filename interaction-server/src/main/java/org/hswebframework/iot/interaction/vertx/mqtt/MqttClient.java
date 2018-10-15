package org.hswebframework.iot.interaction.vertx.mqtt;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.iot.interaction.core.IotCommand;
import org.hswebframework.iot.interaction.vertx.client.Client;

/**
 * @author zhouhao
 * @since 1.1.0
 */
@Slf4j
public class MqttClient implements Client {

    private MqttEndpoint endpoint;

    private long connectTime = System.currentTimeMillis();

    private volatile long lastPingTime = System.currentTimeMillis();

    public MqttClient(MqttEndpoint endpoint) {
        endpoint.pingHandler(r -> ping());
        this.endpoint = endpoint;
    }

    @Override
    public long connectTime() {
        return connectTime;
    }

    @Override
    public String getId() {
        return getClientId();
    }

    @Override
    public String getClientId() {
        return endpoint.clientIdentifier();
    }

    @Override
    public long lastPingTime() {
        return lastPingTime;
    }

    @Override
    public void send(String topic, IotCommand command) {
        endpoint.publish(topic, Buffer.buffer(command.toString()), MqttQoS.AT_MOST_ONCE, false, false);
    }

    @Override
    public void close() {
        if (endpoint.isConnected()) {
            endpoint.close();
        }
    }

    @Override
    public void ping() {
        log.debug("mqtt client[{}] ping", getClientId());
        lastPingTime = System.currentTimeMillis();
    }

    @Override
    public boolean alive() {
        return endpoint.isConnected();
    }

    @Override
    public String toString() {
        return "MQTT Client[" + getClientId() + "]";
    }
}
