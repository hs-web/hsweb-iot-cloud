package org.hswebframework.iot.interaction.vertx.client;


import org.hswebframework.iot.interaction.core.IotCommand;

/**
 * @author zhouhao
 * @since 1.1.0
 */
public interface Client {

    String getId();

    String getClientId();

    long lastPingTime();

    long connectTime();

    void send(String topic, IotCommand command);

    void close();

    void ping();

    boolean alive();
}
