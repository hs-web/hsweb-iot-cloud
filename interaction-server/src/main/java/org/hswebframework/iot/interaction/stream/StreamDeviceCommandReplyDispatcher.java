package org.hswebframework.iot.interaction.stream;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.iot.interaction.events.CommandReplyEvent;
import org.hswebframework.iot.interaction.events.DeviceConnectEvent;
import org.hswebframework.iot.interaction.events.DeviceDisconnectEvent;
import org.hswebframework.iot.interaction.events.DeviceReportEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * 将设备上传的数据上报
 * @author zhouhao
 * @since 1.0.0
 */
@Component
@Slf4j
@EnableBinding
public class StreamDeviceCommandReplyDispatcher {
    @Autowired
    private BinderAwareChannelResolver resolver;

    private long sendTimeout = 10 * 1000L;

    @EventListener
    @Async
    public void handleDeviceOffLineEvent(DeviceDisconnectEvent event) {
        handleSendException(event,
                () -> resolver.resolveDestination("iot.device.disconnect")
                        .send(new GenericMessage<>(JSON.toJSONString(event)), sendTimeout));
    }

    @EventListener
    @Async
    public void handleDeviceOnLineEvent(DeviceConnectEvent event) {
        handleSendException(event,
                () -> resolver.resolveDestination("iot.device.connect")
                        .send(new GenericMessage<>(JSON.toJSONString(event)), sendTimeout));
    }

    @EventListener
    @Async
    public void handleCommandReply(CommandReplyEvent event) {
        handleSendException(event,
                () -> resolver.resolveDestination("iot.command.reply." + event.getOperation())
                        .send(new GenericMessage<>(JSON.toJSONString(event)), sendTimeout));
    }

    @EventListener
    @Async
    public void handleCommandReport(DeviceReportEvent event) {


        handleSendException(event, () ->
                resolver.resolveDestination("iot.device.report." + event.getAction())
                        .send(new GenericMessage<>(JSON.toJSONString(event)), sendTimeout));
    }

    protected void handleSendException(Object event, Supplier<Boolean> job) {
        boolean success;
        Throwable error = null;
        try {
            success = job.get();
        } catch (Throwable throwable) {
            success = false;
            error = throwable;
        }
        if (!success) {
            log.error("send event to spring cloud stream error, payload:\n{}", JSON.toJSONString(event), error);
        } else if (log.isDebugEnabled()) {
            log.debug("send event to spring cloud stream success,payload:\n{}", JSON.toJSONString(event));
        }

    }
}
