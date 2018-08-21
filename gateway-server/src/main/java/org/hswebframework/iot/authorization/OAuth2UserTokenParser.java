package org.hswebframework.iot.authorization;

import org.hswebframework.web.authorization.basic.web.ParsedToken;
import org.hswebframework.web.authorization.basic.web.UserTokenParser;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * 用于OAuth2支持
 *
 * @author zhouhao
 * @since 1.0.0
 */
@Component
public class OAuth2UserTokenParser implements UserTokenParser {

    @Override
    public ParsedToken parseToken(HttpServletRequest request) {
        if (request.getRequestURI().contains("oauth2") && request.getParameter("grant_type") != null) {
            return null;
        }
        String accessToken = request.getHeader("Authorization");
        if (StringUtils.isEmpty(accessToken)) {
            accessToken = request.getParameter("access_token");
        } else {
            String[] arr = accessToken.split("[ ]");
            if (arr.length > 1) {
                accessToken = arr[1];
            }
        }
        if (StringUtils.isEmpty(accessToken)) {
            return null;
        }
        String finalToken = accessToken;

        return new ParsedToken() {

            @Override
            public String getToken() {
                return finalToken;
            }

            @Override
            public String getType() {
                return "oauth2_token";
            }
        };
    }
}
