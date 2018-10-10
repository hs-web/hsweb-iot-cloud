package org.hswebframework.iot.interaction.vertx.cluster;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.shareddata.AsyncMap;
import lombok.AllArgsConstructor;
import org.redisson.api.RMapCacheAsync;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@AllArgsConstructor
public class RedisAsyncMap<K, V> implements AsyncMap<K, V> {

    private RMapCacheAsync<K, V> rMapAsync;

    @Override
    public void get(K k, Handler<AsyncResult<V>> asyncResultHandler) {
        rMapAsync.getAsync(k)
                .thenAccept(v -> asyncResultHandler.handle(Future.succeededFuture(v)))
                .exceptionally(error -> {
                    asyncResultHandler.handle(Future.failedFuture(error));
                    return null;
                });
    }

    @Override
    public void put(K k, V v, Handler<AsyncResult<Void>> completionHandler) {
        rMapAsync.putAsync(k, v).thenAccept(old -> completionHandler.handle(Future.succeededFuture()))
                .exceptionally(error -> {
                    completionHandler.handle(Future.failedFuture(error));
                    return null;
                });
    }

    @Override
    public void put(K k, V v, long ttl, Handler<AsyncResult<Void>> completionHandler) {
        rMapAsync.putAsync(k, v, ttl, TimeUnit.MILLISECONDS)
                .thenAccept(old -> completionHandler.handle(Future.succeededFuture()))
                .exceptionally(error -> {
                    completionHandler.handle(Future.failedFuture(error));
                    return null;
                });
    }

    @Override
    public void putIfAbsent(K k, V v, Handler<AsyncResult<V>> completionHandler) {
        rMapAsync.putIfAbsentAsync(k, v)
                .thenAccept(r -> completionHandler.handle(Future.succeededFuture(r)))
                .exceptionally(error -> {
                    completionHandler.handle(Future.failedFuture(error));
                    return null;
                });
    }

    @Override
    public void putIfAbsent(K k, V v, long ttl, Handler<AsyncResult<V>> completionHandler) {
        rMapAsync.putIfAbsentAsync(k, v, ttl, TimeUnit.MILLISECONDS)
                .thenAccept(r -> completionHandler.handle(Future.succeededFuture(r)))
                .exceptionally(error -> {
                    completionHandler.handle(Future.failedFuture(error));
                    return null;
                });
    }

    @Override
    public void remove(K k, Handler<AsyncResult<V>> asyncResultHandler) {
        rMapAsync.removeAsync(k)
                .thenAccept(v -> asyncResultHandler.handle(Future.succeededFuture(v)))
                .exceptionally(error -> {
                    asyncResultHandler.handle(Future.failedFuture(error));
                    return null;
                });
    }

    @Override
    public void removeIfPresent(K k, V v, Handler<AsyncResult<Boolean>> resultHandler) {
        rMapAsync.removeAsync(k, v)
                .thenAccept(success -> resultHandler.handle(Future.succeededFuture(success)))
                .exceptionally(error -> {
                    resultHandler.handle(Future.failedFuture(error));
                    return null;
                });
    }

    @Override
    public void replace(K k, V v, Handler<AsyncResult<V>> asyncResultHandler) {
        rMapAsync.replaceAsync(k, v)
                .thenAccept(old -> asyncResultHandler.handle(Future.succeededFuture(old)))
                .exceptionally(error -> {
                    asyncResultHandler.handle(Future.failedFuture(error));
                    return null;
                });
    }

    @Override
    public void replaceIfPresent(K k, V oldValue, V newValue, Handler<AsyncResult<Boolean>> resultHandler) {
        removeIfPresent(k, oldValue, result -> {
            if (result.succeeded()) {
                replace(k, newValue, suc -> resultHandler.handle(Future.succeededFuture()));
            } else {
                resultHandler.handle(Future.succeededFuture(false));
            }
        });
    }

    @Override
    public void clear(Handler<AsyncResult<Void>> resultHandler) {
        rMapAsync.deleteAsync()
                .thenAccept(success -> resultHandler.handle(Future.succeededFuture()))
                .exceptionally(error -> {
                    resultHandler.handle(Future.failedFuture(error));
                    return null;
                });
    }

    @Override
    public void size(Handler<AsyncResult<Integer>> resultHandler) {
        rMapAsync.sizeAsync()
                .thenAccept(size -> resultHandler.handle(Future.succeededFuture(size)))
                .exceptionally(error -> {
                    resultHandler.handle(Future.failedFuture(error));
                    return null;
                });
    }

    @Override
    public void keys(Handler<AsyncResult<Set<K>>> asyncResultHandler) {
        rMapAsync.readAllKeySetAsync()
                .thenAccept(keys -> asyncResultHandler.handle(Future.succeededFuture(keys)))
                .exceptionally(error -> {
                    asyncResultHandler.handle(Future.failedFuture(error));
                    return null;
                });
    }

    @Override
    public void values(Handler<AsyncResult<List<V>>> asyncResultHandler) {
        rMapAsync.readAllValuesAsync()
                .thenAccept(list -> asyncResultHandler
                        .handle(Future.succeededFuture(list instanceof List ? ((List<V>) list) : new ArrayList<>(list))))
                .exceptionally(error -> {
                    asyncResultHandler.handle(Future.failedFuture(error));
                    return null;
                });
    }

    @Override
    public void entries(Handler<AsyncResult<Map<K, V>>> asyncResultHandler) {
        rMapAsync.readAllEntrySetAsync()
                .thenAccept(entries -> {
                    Map<K, V> map = new HashMap<>(entries.size());
                    for (Map.Entry<K, V> entry : entries) {
                        map.put(entry.getKey(), entry.getValue());
                    }
                    asyncResultHandler.handle(Future.succeededFuture(map));
                })
                .exceptionally(error -> {
                    asyncResultHandler.handle(Future.failedFuture(error));
                    return null;
                });
    }
}
