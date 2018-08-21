package org.hswebframework.iot.interaction.authority;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface DeviceAuthorityService {
    boolean verification(String clientId, String username, String password);
}
