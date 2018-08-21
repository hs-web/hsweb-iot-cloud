package org.hswebframework.iot.interaction.web;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.iot.interaction.core.IotCommand;
import org.hswebframework.iot.interaction.core.IotCommandSender;
import org.hswebframework.iot.interaction.core.Topics;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@RestController
@RequestMapping("/device/command")
@Slf4j
public class DeviceCommandSendController {

    @Autowired
    private IotCommandSender commandSender;

    @PostMapping("/{clientId}")
    public ResponseMessage<Object> send(@PathVariable String clientId, @RequestBody IotCommand command) {
        try {
            commandSender.send(Topics.execute, clientId, command);
        } catch (BusinessException e) {
            return ResponseMessage.error(e.getStatus(), e.getMessage()).code(e.getCode());
        }
        return ResponseMessage.ok();
    }

}
