package org.hswebframework.iot.interaction.vertx;

import io.vertx.core.Verticle;
import io.vertx.core.spi.VerticleFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @author zhouhao
 * @see VerticleSupplier
 * @since 1.0.0
 */
@Component
public class SpringVerticleFactory implements VerticleFactory {

    @Autowired
    private ApplicationContext context;

    @Override
    public String prefix() {
        return "spring";
    }

    @Override
    public Verticle createVerticle(String verticleName, ClassLoader classLoader) throws Exception {
        verticleName = VerticleFactory.removePrefix(verticleName);
        return context.getBean(verticleName, Verticle.class);
    }
}
