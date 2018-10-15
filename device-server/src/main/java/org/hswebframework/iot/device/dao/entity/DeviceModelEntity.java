package org.hswebframework.iot.device.dao.entity;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class DeviceModelEntity extends SimpleGenericEntity<String> {

    private String typeId;
    
    private String name;

    private String supportOperationJson;
}
