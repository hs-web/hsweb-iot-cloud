package org.hswebframework.iot.interaction.core;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface Topics {

    /**
     * 服务向客户端发送执行命令消息的topic
     */
    String execute = "execute";

    /**
     * 客户端接收到execute命令向服务端返回结果时使用的topic
     */
    String reply = "reply";

    /**
     * 客户端直接上报数据时使用的topic
     */
    String report = "report";
}
