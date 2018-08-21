package org.hswebframework.iot.user.config;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.basic.web.GeneratedToken;
import org.hswebframework.web.authorization.basic.web.ParsedToken;
import org.hswebframework.web.authorization.basic.web.UserTokenGenerator;
import org.hswebframework.web.authorization.basic.web.UserTokenParser;
import org.hswebframework.web.id.IDGenerator;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * @author zhouhao
 * @since 1.0
 */
@Component
@ConfigurationProperties("iot-cloud-user")
public class IotUserTokenGenerator implements UserTokenGenerator, UserTokenParser {
    private int timeout = 60 * 60 * 1000;

    @Override
    public String getSupportTokenType() {
        return "iot-cloud-user";
    }

    @Override
    public GeneratedToken generate(Authentication authentication) {
        String token = UUID.randomUUID() + "-" + IDGenerator.SNOW_FLAKE_HEX.generate();

        return new GeneratedToken() {
            @Override
            public Map<String, Object> getResponse() {
                return Collections.singletonMap("token", token);
            }

            @Override
            public String getToken() {
                return token;
            }

            @Override
            public String getType() {
                return getSupportTokenType();
            }

            @Override
            public int getTimeout() {
                return timeout;
            }
        };
    }

    @Override
    public ParsedToken parseToken(HttpServletRequest request) {
        String header = request.getHeader("iot-cloud-user");
        if (StringUtils.isEmpty(header)) {
            return null;
        }
        return new ParsedToken() {
            @Override
            public String getToken() {
                return header;
            }

            @Override
            public String getType() {
                return getSupportTokenType();
            }
        };
    }
}
