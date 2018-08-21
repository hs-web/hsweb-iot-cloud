package org.hswebframework.iot.interaction.authority;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Service
@Slf4j
public class DeviceDeviceAuthorityService implements DeviceAuthorityService {

    @Override
    public boolean verification(String clientId, String username, String password) {
        // TODO: 18-8-21 验证设备权限
        return true;
    }
}
