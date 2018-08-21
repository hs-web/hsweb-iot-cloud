package org.hswebframework.iot.logging.provider;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import org.hswebframework.iot.logging.AccessLoggerRequest;
import org.hswebframework.iot.logging.AccessLoggerResponse;
import org.hswebframework.iot.logging.SystemLoggingInfo;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

/**
 * @author zhouhao
 * @since 1.0
 */
@AllArgsConstructor
public class RedisLoggingProvider {

    private RedissonClient redissonClient;

    @EventListener
    @Async
    public void handleAccessLoggerRequest(AccessLoggerRequest request) {
        redissonClient
                .getTopic("iot-access-logger-request", new StringCodec())
                .publish(JSON.toJSONString(request));
    }

    @EventListener
    @Async
    public void handleAccessLoggerResponse(AccessLoggerResponse response) {
        redissonClient
                .getTopic("iot-access-logger-response", new StringCodec())
                .publish(JSON.toJSONString(response));
    }

    @EventListener
    @Async
    public void handleSystemLogger(SystemLoggingInfo info) {
        redissonClient
                .getTopic("iot-system-logger", new StringCodec())
                .publish(JSON.toJSONString(info));
    }
}
