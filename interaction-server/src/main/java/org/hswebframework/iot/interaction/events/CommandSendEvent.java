package org.hswebframework.iot.interaction.events;

import com.alibaba.fastjson.JSON;
import lombok.*;
import org.hswebframework.iot.interaction.core.IotCommand;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CommandSendEvent {
    private String topic;

    private String clientId;

    private IotCommand command;

    public String toJSONString() {
        return JSON.toJSONString(this);
    }

    public String toString() {
        return toJSONString();
    }
}
