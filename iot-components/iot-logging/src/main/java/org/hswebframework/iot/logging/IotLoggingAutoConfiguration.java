package org.hswebframework.iot.logging;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author zhouhao
 * @since 1.0
 */
@Configuration
public class IotLoggingAutoConfiguration implements ApplicationEventPublisherAware, EnvironmentAware {

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        IotLogAppender.setPublisher(applicationEventPublisher);
    }

    @Override
    public void setEnvironment(Environment environment) {
        IotLogAppender.appId = environment.getProperty("hsweb.app.name", environment.getProperty("spring.application.name"));

    }
}
