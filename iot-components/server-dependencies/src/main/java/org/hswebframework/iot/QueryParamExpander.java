package org.hswebframework.iot;

import org.hswebframework.web.WebUtil;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.springframework.cloud.netflix.feign.FeignFormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;


/**
 * @author zhouhao
 * @since 1.0
 */
@Component
public class QueryParamExpander implements FeignFormatterRegistrar {

    @Override
    public void registerFormatters(FormatterRegistry registry) {
        registry.addConverter(QueryParamEntity.class, String.class, source ->
                WebUtil.objectToHttpParameters(source).entrySet()
                        .stream().filter(entry -> entry.getValue() != null)
                        .map(entry -> entry.getKey().concat("=").concat(entry.getValue()))
                        .reduce((s1, s2) -> s1.concat("&").concat(s2))
                        .orElse(""));
    }
}
