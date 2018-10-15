package org.hswebframework.iot.device.dao.entity;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.iot.device.enums.DeviceStatusEnum;
import org.hswebframework.web.bean.ToString;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class DeviceEntity extends SimpleGenericEntity<String> {
    private String typeId;

    private String modelId;

    private String name;

    private String comments;

    private String group;

    private String gatewayId;

    private String sn;

    private String secretId;

    @ToString.Ignore
    private String secretKey;

    private String ipAddress;

    private DeviceStatusEnum status;

    private String protocol;

    private String creatorId;

    private Long createTime;

    private Long lastUpdateTime;

    private Long lastStatusChangeTime;

}
