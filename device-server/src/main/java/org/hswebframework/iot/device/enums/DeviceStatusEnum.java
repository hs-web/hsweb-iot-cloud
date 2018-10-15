package org.hswebframework.iot.device.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.dict.Dict;
import org.hswebframework.web.dict.EnumDict;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@AllArgsConstructor
@Getter
@Dict(id = "device-status")
public enum DeviceStatusEnum implements EnumDict<Byte> {

    unregister((byte) -2, "未注册"),
    online((byte) 1, "在线"),
    offline((byte) -1, "离线"),
    ;
    private Byte value;

    private String text;
}
