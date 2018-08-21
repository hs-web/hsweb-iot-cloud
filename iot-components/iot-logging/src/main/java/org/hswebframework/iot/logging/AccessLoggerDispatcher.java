package org.hswebframework.iot.logging;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.ThreadLocalUtils;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.logging.events.AccessLoggerAfterEvent;
import org.hswebframework.web.logging.events.AccessLoggerBeforeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 访问日志转发
 *
 * @author zhouhao
 * @since 1.0
 */
@Component
@Slf4j
public class AccessLoggerDispatcher {

    @Autowired
    public ApplicationEventPublisher publisher;

    @Value("${spring.application.name:unknown}")
    private String serviceId = "unknown";

    private static final Class excludes[] = {
            ServletRequest.class,
            ServletResponse.class,
            InputStream.class,
            OutputStream.class,
            MultipartFile.class,
            MultipartFile[].class
    };

    @EventListener
    public void handleLoggingBefore(AccessLoggerBeforeEvent event) {
        //获取全局请求ID
        String requestId = event.getLogger()
                .getHttpHeaders()
                .getOrDefault("request-id", String.valueOf(ThreadLocalUtils.get("request-id", IDGenerator.SNOW_FLAKE_STRING::generate)));

        //转换日志信息
        AccessLoggerRequest info = new AccessLoggerRequest();
        Map<String, Object> logMap = event.getLogger().toSimpleMap(obj -> {
            if (Stream.of(excludes).anyMatch(aClass -> aClass.isInstance(obj))) {
                return obj.getClass().getName();
            }
            return (Serializable) JSON.toJSON(obj);
        });
        FastBeanCopier.copy(logMap, info, "id");
        info.setId(IDGenerator.SNOW_FLAKE_STRING.generate());
        info.setRequestId(requestId);
        info.setLogId(event.getLogger().getId());
        info.setServiceId(serviceId);
        try {
            info.setServiceHost(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException ignore) {
        }
        ThreadLocalUtils.put("access-logger-id", info.getId());
        publisher.publishEvent(info);
    }

    @EventListener
    public void handleLoggingAfter(AccessLoggerAfterEvent event) {
        String id = ThreadLocalUtils.get("access-logger-id");
        if (id == null) {
            log.warn("无法获取访问日志ID,日志:{}", event.getLogger().getId());
            return;
        }
        publisher.publishEvent(new AccessLoggerResponse(id, event.getLogger().getResponseTime(), event.getLogger().getResponse()));
    }
}
