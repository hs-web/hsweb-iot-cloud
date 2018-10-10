package org.hswebframework.iot.interaction.vertx.cluster;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NodeChangedEvent implements Serializable {
    private EventType type;

    private String nodeId;

    enum EventType {
        join, leave
    }
}
