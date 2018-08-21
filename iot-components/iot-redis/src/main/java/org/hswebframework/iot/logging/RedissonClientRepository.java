package org.hswebframework.iot.logging;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Optional;

/**
 * @author zhouhao
 * @since 1.0
 */
public class RedissonClientRepository {

    @Autowired
    private Map<String, RedissonClient> repository;

    @Autowired
    private RedissonClient defaultRedissonClient;

    public Optional<RedissonClient> getClient(String name) {
        return Optional.ofNullable(repository.get(name));
    }

    public RedissonClient getDefaultClient() {
        return defaultRedissonClient;
    }
}
