package org.hswebframework.iot.interaction.core;

/**
 * 命令发送器
 *
 * @author zhouhao
 * @since 1.0.0
 */
public interface IotCommandSender {
    /**
     * 发送命令
     *
     * @param clientId 客户端Id
     * @param command  命令
     */
    void send(String topic, String clientId, IotCommand command);
}
