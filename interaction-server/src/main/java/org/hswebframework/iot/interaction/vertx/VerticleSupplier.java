package org.hswebframework.iot.interaction.vertx;

import io.vertx.core.Verticle;
import io.vertx.core.VertxOptions;

import java.util.function.Supplier;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface VerticleSupplier extends Supplier<Verticle> {
    default int getInstances() {
        return VertxOptions.DEFAULT_EVENT_LOOP_POOL_SIZE;
    }
}
