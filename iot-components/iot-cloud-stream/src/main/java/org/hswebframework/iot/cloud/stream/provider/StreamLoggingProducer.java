package org.hswebframework.iot.cloud.stream.provider;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.iot.logging.AccessLoggerRequest;
import org.hswebframework.iot.logging.AccessLoggerResponse;
import org.hswebframework.iot.logging.SystemLoggingInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Async;

/**
 * @author zhouhao
 * @since 1.0
 */
@Slf4j
public class StreamLoggingProducer {

    @Autowired(required = false)
    private LoggerProducer producer;

    @EventListener
    @Async
    public void handleAccessLoggerRequest(AccessLoggerRequest request) {
        try {
            log.info("send access logger request [id={},request-id={}] to kafka", request.getId(), request.getRequestId());
            producer.accessLoggerRequest().send(MessageBuilder.withPayload(JSON.toJSONString(request)).build());
        } catch (Exception e) {
            log.error("send access logger request error:\n{}", JSON.toJSONString(request), e);
        }
    }

    @EventListener
    @Async
    public void handleAccessLoggerResponse(AccessLoggerResponse response) {
        try {
            log.info("send access logger response [{}] to kafka", response.getId());
            producer.accessLoggerResponse().send(MessageBuilder.withPayload(JSON.toJSONString(response)).build());
        } catch (Exception e) {
            log.error("send access logger request error:\n{}", JSON.toJSONString(response), e);
        }
    }

    @EventListener
    @Async
    public void handleSystemLogger(SystemLoggingInfo info) {
        try {
            log.info("send business logger [{}] to kafka", info.getId());
            producer.businessLogger()
                    .send(MessageBuilder.withPayload(JSON.toJSONString(info))
                            .build());
        } catch (Exception e) {
            log.error("send business logger error:\n{}", JSON.toJSONString(info), e);
        }
    }

}
