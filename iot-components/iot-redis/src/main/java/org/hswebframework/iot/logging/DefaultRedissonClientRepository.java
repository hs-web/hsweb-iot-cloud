package org.hswebframework.iot.logging;

import lombok.Getter;
import lombok.Setter;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author zhouhao
 * @since 1.1.0
 */
public class DefaultRedissonClientRepository implements RedissonClientRepository {
    @Getter
    @Setter
    private Map<String, RedissonProperties> clients = new HashMap<>();

    private Map<String, RedissonClient> repository = new HashMap<>();

    @PostConstruct
    public void init() {
        for (Map.Entry<String, RedissonProperties> entry : clients.entrySet()) {
            repository.put(entry.getKey(), Redisson.create(entry.getValue().toConfig(clients.get("default"))));
        }
    }

    public Optional<RedissonClient> getClient(String name) {
        return Optional.ofNullable(repository.get(name));
    }

}
