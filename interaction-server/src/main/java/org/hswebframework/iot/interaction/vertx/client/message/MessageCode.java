package org.hswebframework.iot.interaction.vertx.client.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@AllArgsConstructor
@Getter
public enum MessageCode {
    SUCCESS(0, "成功"),

    //
    UN_REGISTERED_CLIENT(40301, "未注册的客户端"),

    NO_AUTH_PARAM(40101, "授权参数错误"),
    AUTH_FAIL(40102, "授权失败"),
    UN_SUPPORT_TYPE(40001, "不支持的操作类型"),
    PARAM_FORMAT_ERROR(40002, "参数格式错误");
    private int code;

    private String message;
}
