package org.hswebframework.iot.device.dao.entity;

import org.hswebframework.iot.device.enums.StatusChangeWay;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class DeviceStatusEntity extends SimpleGenericEntity<String> {

    private String gatewayId;

    private String deviceId;

    /**
     * @see org.hswebframework.iot.device.operation.DeviceOperation#key
     */
    private String operationKey;

    /**
     * @see org.hswebframework.iot.device.operation.OperationValue#name
     */
    private String valueName;

    /**
     * @see org.hswebframework.iot.device.operation.OperationValue#value
     */
    private String value;

    private Long lastChangeTime;

    private StatusChangeWay changeWay;

}
