package org.hswebframework.iot.logging;

import org.redisson.api.RedissonClient;

import java.util.Optional;

/**
 * @author zhouhao
 * @since 1.0
 */
public interface RedissonClientRepository {

    Optional<RedissonClient> getClient(String name);

    default RedissonClient getDefaultClient() {
        return getClient("default").orElseThrow(NullPointerException::new);
    }
}
