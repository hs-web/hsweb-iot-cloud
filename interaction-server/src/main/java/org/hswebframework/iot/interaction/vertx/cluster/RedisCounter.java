package org.hswebframework.iot.interaction.vertx.cluster;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.shareddata.Counter;
import lombok.AllArgsConstructor;
import org.redisson.api.RAtomicLongAsync;

/**
 * @author zhouhao
 * @since 1.0
 */
@AllArgsConstructor
public class RedisCounter implements Counter {

    private RAtomicLongAsync counter;


    @Override
    public void get(Handler<AsyncResult<Long>> resultHandler) {
        counter.getAsync()
                .thenAccept(result -> resultHandler.handle(Future.succeededFuture(result)))
                .exceptionally(error -> {
                    resultHandler.handle(Future.failedFuture(error));
                    return null;
                });
    }

    @Override
    public void incrementAndGet(Handler<AsyncResult<Long>> resultHandler) {
        counter.incrementAndGetAsync()
                .thenAccept(result -> resultHandler.handle(Future.succeededFuture(result)))
                .exceptionally(error -> {
                    resultHandler.handle(Future.failedFuture(error));
                    return null;
                });
    }

    @Override
    public void getAndIncrement(Handler<AsyncResult<Long>> resultHandler) {
        counter.getAndIncrementAsync()
                .thenAccept(result -> resultHandler.handle(Future.succeededFuture(result)))
                .exceptionally(error -> {
                    resultHandler.handle(Future.failedFuture(error));
                    return null;
                });
    }

    @Override
    public void decrementAndGet(Handler<AsyncResult<Long>> resultHandler) {
        counter.decrementAndGetAsync()
                .thenAccept(result -> resultHandler.handle(Future.succeededFuture(result)))
                .exceptionally(error -> {
                    resultHandler.handle(Future.failedFuture(error));
                    return null;
                });
    }

    @Override
    public void addAndGet(long value, Handler<AsyncResult<Long>> resultHandler) {
        counter.addAndGetAsync(value)
                .thenAccept(result -> resultHandler.handle(Future.succeededFuture(result)))
                .exceptionally(error -> {
                    resultHandler.handle(Future.failedFuture(error));
                    return null;
                });
    }

    @Override
    public void getAndAdd(long value, Handler<AsyncResult<Long>> resultHandler) {
        counter.getAndAddAsync(value)
                .thenAccept(result -> resultHandler.handle(Future.succeededFuture(result)))
                .exceptionally(error -> {
                    resultHandler.handle(Future.failedFuture(error));
                    return null;
                });
    }

    @Override
    public void compareAndSet(long expected, long value, Handler<AsyncResult<Boolean>> resultHandler) {
        counter.compareAndSetAsync(expected, value)
                .thenAccept(result -> resultHandler.handle(Future.succeededFuture(result)))
                .exceptionally(error -> {
                    resultHandler.handle(Future.failedFuture(error));
                    return null;
                });
    }
}
