package org.hswebframework.iot.logging;

import java.util.concurrent.TimeUnit;

/**
 * @author wangzheng
 * @since 1.0
 */
public interface SyncOperation<T> {

    T get(long timeout, TimeUnit timeUnit);

    default T getAndClear(long timeout, TimeUnit timeUnit) {
        try {
            return get(timeout, timeUnit);
        }finally {
            clear();
        }
    }

    void done(T data);

    void clear();
}
