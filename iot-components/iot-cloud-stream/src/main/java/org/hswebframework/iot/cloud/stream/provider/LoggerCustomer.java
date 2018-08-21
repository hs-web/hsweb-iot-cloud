package org.hswebframework.iot.cloud.stream.provider;


import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

import static org.hswebframework.iot.cloud.stream.provider.LoggerProducer.*;


/**
 * @author zhouhao
 * @since 3.0
 */
public interface LoggerCustomer {

    @Input(ACCESS_LOGGER_REQUEST)
    SubscribableChannel accessLoggerRequest();
    
    @Input(ACCESS_LOGGER_RESPONSE)
    SubscribableChannel accessLoggerResponse();

    @Input(ACCESS_LOGGER_SYSTEM)
    SubscribableChannel systemLogger();

}
