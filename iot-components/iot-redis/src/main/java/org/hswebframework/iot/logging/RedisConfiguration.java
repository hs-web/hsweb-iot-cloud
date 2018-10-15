package org.hswebframework.iot.logging;

import org.hswebframework.web.authorization.token.DefaultUserTokenManager;
import org.hswebframework.web.authorization.token.SimpleUserToken;
import org.hswebframework.web.authorization.token.UserToken;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.nustaq.serialization.FSTConfiguration;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;
import org.redisson.codec.FstCodec;
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
public class RedisConfiguration {

    @Value("${iot.redis.user-token-client-name:defaultRedissonClient}")
    private String userTokenClientName = "defaultRedissonClient";

    @Bean
    @Primary
    public RedissonClient defaultRedissonClient(RedissonClientRepository repository) {
        return repository.getDefaultClient();
    }

    @Bean
    @ConfigurationProperties(prefix = "iot.redis", ignoreInvalidFields = true)
    public RedissonClientRepository redissonClientRepository() {
        return new DefaultRedissonClientRepository();
    }

    @Bean
    public Codec fstCodec() {
        FSTConfiguration def = FSTConfiguration.createDefaultConfiguration();
        def.setClassLoader(this.getClass().getClassLoader());
        def.setForceSerializable(true);
        StringCodec stringCodec = new StringCodec();
        return new FstCodec(def) {
            @Override
            public Decoder<Object> getMapKeyDecoder() {
                return stringCodec.getMapKeyDecoder();
            }

            @Override
            public Encoder getMapKeyEncoder() {
                return stringCodec.getMapKeyEncoder();
            }
        };
    }

    @Bean
    public CacheManager cacheManager(RedissonClientRepository repository) {
        RedissonSpringCacheManager cacheManager = new RedissonSpringCacheManager(repository.getDefaultClient());
        cacheManager.setCodec(fstCodec());
        return new TransactionAwareCacheManagerProxy(cacheManager);
    }

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
                return client.getSet("hsweb-iot-cloud.user-token-" + userId, codec);
            }

            @Override
            protected void syncToken(UserToken userToken) {
                tokenStorage.put(userToken.getToken(), (SimpleUserToken) userToken);
            }
        };
    }
}
