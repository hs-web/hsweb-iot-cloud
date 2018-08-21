package org.hswebframework.iot.cloud.stream.provider;


import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

/**
 * @author zhouhao
 * @since 3.0
 */
public interface LoggerProducer {
    String ACCESS_LOGGER_REQUEST  = "iot-cloud-accessLoggerRequest";
    String ACCESS_LOGGER_RESPONSE = "iot-cloud-accessLoggerResponse";
    String ACCESS_LOGGER_SYSTEM   = "iot-cloud-systemLogger";

    @Output(ACCESS_LOGGER_REQUEST)
    MessageChannel accessLoggerRequest();

    @Output(ACCESS_LOGGER_RESPONSE)
    MessageChannel accessLoggerResponse();

    @Output(ACCESS_LOGGER_SYSTEM)
    MessageChannel businessLogger();

}
