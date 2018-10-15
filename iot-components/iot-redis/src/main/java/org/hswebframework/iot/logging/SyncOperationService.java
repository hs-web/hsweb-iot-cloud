package org.hswebframework.iot.logging;

import lombok.SneakyThrows;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 *     syncOperationService.getSyncOperation("remote-download")
 *      .onDone((data)->{
 *
 *      })
 * </pre>
 *
 * @author zhouhao
 * @since 1.0
 */
@Component
public class SyncOperationService {
    @Autowired
    private RedissonClient redissonClient;

    public <T> SyncOperation<T> getSyncOperation(String key, String id) {
        Map<String, T> map = redissonClient.getMap("sync-operation-" + key);
        RCountDownLatch countDownLatch = redissonClient.getCountDownLatch("sync-operation-count-down-" + key + "-" + id);
        countDownLatch.trySetCount(1);
        RAtomicLong waiting = redissonClient.getAtomicLong("sync-operation-waiting-" + key + "-" + id);

        return new SyncOperation<T>() {
            @Override
            @SneakyThrows
            public T get(long timeout, TimeUnit timeUnit) {
                waiting.set(1L);
                try {
                    countDownLatch.await(timeout, timeUnit);
                } finally {
                    waiting.set(0);
                }
                return map.get(id);
            }

            @Override
            public void clear() {
                waiting.delete();
                map.remove(id);
                countDownLatch.delete();
            }

            @Override
            public void done(T data) {
                if (waiting.get() == 0) {
                    clear();
                    return;
                }
                try {
                    map.put(id, data);
                } finally {
                    countDownLatch.countDown();

                }
            }
        };
    }
}
