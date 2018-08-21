package org.hswebframework.iot.interaction.vertx.udp;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.iot.interaction.vertx.client.message.ClientMessage;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class UDPMessage extends ClientMessage {

    private String type;

}
