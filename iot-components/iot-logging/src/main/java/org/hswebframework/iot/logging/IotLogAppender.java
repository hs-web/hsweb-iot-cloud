package org.hswebframework.iot.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.ThreadLocalUtils;
import org.hswebframework.web.id.IDGenerator;
import org.springframework.context.ApplicationEventPublisher;

import java.util.StringJoiner;


@Slf4j
public class IotLogAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private static ApplicationEventPublisher publisher;

    static String appId = "default";

    static void setPublisher(ApplicationEventPublisher publisher) {
        IotLogAppender.publisher = publisher;
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (publisher == null) {
            return;
        }
        StackTraceElement element = event.getCallerData()[0];
        IThrowableProxy proxies = event.getThrowableProxy();
        String message = event.getFormattedMessage();
        String stack = null;

        if (null != proxies) {
            int commonFrames = proxies.getCommonFrames();
            StackTraceElementProxy[] stepArray = proxies.getStackTraceElementProxyArray();
            StringJoiner joiner = new StringJoiner("\n", message + "\n[", "]");

            StringBuilder stringBuilder = new StringBuilder();
            ThrowableProxyUtil.subjoinFirstLine(stringBuilder, proxies);
            joiner.add(stringBuilder);
            for (int i = 0; i < stepArray.length - commonFrames; i++) {
                StringBuilder sb = new StringBuilder();
                sb.append(CoreConstants.TAB);
                ThrowableProxyUtil.subjoinSTEP(sb, stepArray[i]);
                joiner.add(sb);
            }
            stack = joiner.toString();
        }
        String requestId = ThreadLocalUtils.get("request-id");
        SystemLoggingInfo info = SystemLoggingInfo.builder()
                .id(IDGenerator.SNOW_FLAKE.generate())
                .requestId(requestId)
                .appId(appId)
                .level(event.getLevel().levelStr)
                .name(event.getLoggerName())
                .className(element.getClassName())
                .methodName(element.getMethodName())
                .lineNumber(element.getLineNumber())
                .message(message)
                .exceptionStack(stack)
                .threadName(event.getThreadName())
                .createTime(event.getTimeStamp())
                .threadId(String.valueOf(Thread.currentThread().getId()))
                .build();
        publisher.publishEvent(info);
    }
}
