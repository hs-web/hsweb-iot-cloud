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
@Dict(id = "status-change-way")
public enum StatusChangeWay implements EnumDict<Byte> {
    report((byte) 1, "设备上报");

    private byte   value;
    private String text;
}
