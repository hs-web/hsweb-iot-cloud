package org.hswebframework.iot.interaction.vertx.cluster;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.spi.cluster.AsyncMultiMap;
import io.vertx.core.spi.cluster.ChoosableIterable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMultimapAsync;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * @author zhouhao
 * @since 1.1.0
 */
@Slf4j
@AllArgsConstructor
public class RedisAsyncMultiMap<K, V> implements AsyncMultiMap<K, V> {

    RMultimapAsync<K, V> redisMap;

    @Override
    public void add(K k, V v, Handler<AsyncResult<Void>> completionHandler) {
        redisMap.putAsync(k, v).
                thenAccept(t -> completionHandler.handle(Future.succeededFuture()));
    }

    @Override
    public void get(K k, Handler<AsyncResult<ChoosableIterable<V>>> asyncResultHandler) {
        redisMap.getAllAsync(k)
                .thenAccept(v -> {
                    Iterator<V> iterator = v.iterator();
                    asyncResultHandler.handle(Future.succeededFuture(new ChoosableIterable<V>() {
                        @Override
                        public boolean isEmpty() {
                            return !iterator.hasNext();
                        }

                        @Override
                        public V choose() {
                            if (iterator.hasNext()) {
                                return iterator.next();
                            }
                            return null;
                        }

                        @Override
                        public Iterator<V> iterator() {
                            return iterator;
                        }
                    }));
                })
                .exceptionally(error -> {
                    asyncResultHandler.handle(Future.failedFuture(error));
                    return null;
                });
    }

    @Override
    public void remove(K k, V v, Handler<AsyncResult<Boolean>> completionHandler) {
        redisMap.removeAsync(k, v)
                .thenAccept(t -> completionHandler.handle(Future.succeededFuture(t)))
                .exceptionally(error -> {
                    completionHandler.handle(Future.failedFuture(error));
                    return null;
                });
    }

    @Override
    public void removeAllForValue(V v, Handler<AsyncResult<Void>> completionHandler) {
        removeAllMatching(val -> val.equals(v), completionHandler);
    }

    @Override
    public void removeAllMatching(Predicate<V> p, Handler<AsyncResult<Void>> completionHandler) {
        redisMap.readAllKeySetAsync()
                .thenAccept(keys -> {
                    if (keys.isEmpty()) {
                        completionHandler.handle(Future.succeededFuture());
                        return;
                    }
                    try {
                        @SuppressWarnings("all")
                        K[] matchedKeys = (K[]) keys
                                .parallelStream()
                                .map(key -> {
                                    Entry entry = new Entry();
                                    entry.key = key;
                                    try {
                                        entry.value = redisMap.getAllAsync(key).get(10, TimeUnit.SECONDS);
                                    } catch (Exception e) {
                                        log.error("get redis map values [{}] error", key, e);
                                        entry.value = Collections.emptyList();
                                    }
                                    return entry;
                                }).filter(entry -> entry.value.stream().anyMatch(p))
                                .map(Entry::getKey)
                                .toArray();
                        if (matchedKeys.length > 0) {
                            redisMap.fastRemoveAsync(matchedKeys)
                                    .thenAccept(len -> completionHandler.handle(Future.succeededFuture()));
                        } else {
                            completionHandler.handle(Future.succeededFuture());
                        }
                    } catch (Exception e) {
                        completionHandler.handle(Future.failedFuture(e));
                    }
                })
                .exceptionally(error -> {
                    completionHandler.handle(Future.failedFuture(error));
                    return null;
                });
    }

    @Getter
    @Setter
    class Entry {
        K             key;
        Collection<V> value;
    }
}
