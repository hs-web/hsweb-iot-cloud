package org.hswebframework.iot.interaction.vertx.client;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface ClientMessageHandler<T> {
    void handle(T message);
}
