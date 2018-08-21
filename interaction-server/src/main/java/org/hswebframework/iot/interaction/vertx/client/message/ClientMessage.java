package org.hswebframework.iot.interaction.vertx.client.message;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Optional;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class ClientMessage {
    private long messageId;

    private int code;

    private String clientId;

    private Map<String, Object> data;

    public Optional<Object> getData(String key) {
        return Optional.ofNullable(data == null ? null : data.get(key));
    }

    public String getStringData(String key) {
        return getData(key).map(String::valueOf).orElse(null);
    }

}
