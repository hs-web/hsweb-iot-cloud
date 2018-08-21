package org.hswebframework.iot.interaction.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@AllArgsConstructor
@Getter
public class DeviceDisconnectEvent {
    private String clientId;

    private Date disconnectTime;
}
