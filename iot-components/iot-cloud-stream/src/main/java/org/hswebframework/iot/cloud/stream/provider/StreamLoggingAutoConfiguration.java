package org.hswebframework.iot.cloud.stream.provider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 * @since 1.0
 */
@Configuration
@ConditionalOnProperty(prefix = "iot.logger.stream", name = "producer", havingValue = "true", matchIfMissing = true)
@EnableBinding(LoggerProducer.class)
public class StreamLoggingAutoConfiguration {

    @Bean
    public StreamLoggingProducer streamLoggingProducer() {
        return new StreamLoggingProducer();
    }


}
