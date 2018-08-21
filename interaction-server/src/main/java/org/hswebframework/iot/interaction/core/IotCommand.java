package org.hswebframework.iot.interaction.core;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;
import org.hswebframework.web.commons.bean.ValidateBean;

import java.util.Map;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
public class IotCommand implements ValidateBean {
    @Range(min = Short.MAX_VALUE,message = "[messageId]值不能小于32767")
    private long messageId;

    @NotBlank(message = "[operation]不能为空")
    private String operation;

    private Map<String, Object> parameters;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
