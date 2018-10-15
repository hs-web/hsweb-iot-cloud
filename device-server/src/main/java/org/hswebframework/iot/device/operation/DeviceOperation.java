package org.hswebframework.iot.device.operation;

import org.hswebframework.web.commons.bean.ValidateBean;

import java.util.List;

/**
 * 设备的操作配置
 *
 * @author zhouhao
 * @since 1.0.0
 */
public class DeviceOperation implements ValidateBean {
    private String key;

    private String name;

    private String valueType;

    private String defaultValue;

    private List<OperationValue> values;
}
