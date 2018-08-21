package org.hswebframework.iot.interaction.events;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hswebframework.iot.interaction.vertx.client.message.ClientMessage;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
@ToString
public class CommandReplyEvent extends ClientMessage {
    private String operation;
}
