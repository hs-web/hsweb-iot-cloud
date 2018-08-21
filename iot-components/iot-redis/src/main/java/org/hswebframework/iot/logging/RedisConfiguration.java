package org.hswebframework.iot.logging;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.iot.logging.provider.RedisLoggingProvider;
import org.hswebframework.web.authorization.token.DefaultUserTokenManager;
import org.hswebframework.web.authorization.token.SimpleUserToken;
import org.hswebframework.web.authorization.token.UserToken;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.nustaq.serialization.FSTConfiguration;
import org.redisson.Redisson;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;
import org.redisson.codec.FstCodec;
import org.redisson.config.Config;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheManagerProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * @author zhouhao
 * @since 1.0
 */
@Configuration
@ConfigurationProperties(prefix = "iot.redis.default")
@ConditionalOnProperty(prefix = "iot.redis", name = "enable", havingValue = "true")
public class RedisConfiguration {

    @Setter
    @Getter
    private String host = "redis://127.0.0.1:6379";

    @Getter
    @Setter
    private int database = 0;

    @Setter
    @Getter
    private String password;

    @Value("${iot.redis.user-token-client-name:defaultRedissonClient}")
    private String userTokenClientName = "defaultRedissonClient";

    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "iot.redis.default", name = "enable", havingValue = "true", matchIfMissing = true)
    public RedissonClient defaultRedissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress(host)
                .setPassword(password)
                .setConnectionPoolSize(128)
                .setDatabase(database);
        return Redisson.create(config);
    }

    @Value("${iot.redis.access-logger-client-name:defaultRedissonClient}")
    private String accessLoggerClientName = "defaultRedissonClient";

    @Bean
    @ConditionalOnProperty(prefix = "iot.redis.logging", name = "enable", havingValue = "true")
    public RedisLoggingProvider redisLoggingProvider(RedissonClientRepository repository) {
        return new RedisLoggingProvider(repository.getClient(accessLoggerClientName)
                .orElseGet(repository::getDefaultClient));
    }

    @Bean
    public RedissonClientRepository redissonClientRepository() {
        return new RedissonClientRepository();
    }

    @Bean
    public Codec fstCodec() {
        FSTConfiguration def = FSTConfiguration.createDefaultConfiguration();
        def.setClassLoader(this.getClass().getClassLoader());
        def.setForceSerializable(true);
        return new FstCodec(def) {
            @Override
            public Decoder<Object> getMapKeyDecoder() {
                return StringCodec.INSTANCE.getMapKeyDecoder();
            }

            @Override
            public Encoder getMapKeyEncoder() {
                return StringCodec.INSTANCE.getMapKeyEncoder();
            }
        };
    }

    @Bean
    public CacheManager cacheManager(RedissonClientRepository repository) {
        RedissonSpringCacheManager cacheManager = new RedissonSpringCacheManager(repository.getDefaultClient());
        cacheManager.setCodec(fstCodec());
        //支持事务的缓存管理器,在事务提交后才会进行缓存相关操作
        return new TransactionAwareCacheManagerProxy(cacheManager);
    }

    /**
     * 使用redis作为user token管理,实现集群用户权限共享
     */
    @Bean
    @ConditionalOnProperty(prefix = "iot.redis.user-token", name = "enable", havingValue = "true", matchIfMissing = true)
    public UserTokenManager userTokenManager(RedissonClientRepository repository) {
        LocalCachedMapOptions<String, SimpleUserToken> localCachedMapOptions =
                LocalCachedMapOptions.<String, SimpleUserToken>defaults()
                        .maxIdle(10, TimeUnit.MINUTES)
                        .timeToLive(5, TimeUnit.MINUTES)
                        .cacheSize(2048);
        Codec codec = fstCodec();
        RedissonClient client = repository.getClient(userTokenClientName).orElseGet(repository::getDefaultClient);
        ConcurrentMap<String, SimpleUserToken> repo = client.getMap("hsweb-iot-cloud.user-token", codec, localCachedMapOptions);
        ConcurrentMap<String, Set<String>> userRepo = client.getMap("hsweb-iot-cloud.user-token-user", codec);

        return new DefaultUserTokenManager(repo, userRepo) {
            @Override
            protected Set<String> getUserToken(String userId) {
                userRepo.computeIfAbsent(userId, u -> new HashSet<>());
                return client.getSet("iot.user-token-" + userId, codec);
            }

            @Override
            protected void syncToken(UserToken userToken) {
                tokenStorage.put(userToken.getToken(), (SimpleUserToken) userToken);
            }
        };
    }
}
