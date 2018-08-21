package org.hswebframework.iot.authorization;

import org.hswebframework.web.authorization.basic.web.ParsedToken;
import org.hswebframework.web.authorization.basic.web.UserTokenParser;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.stream.Stream;

import static org.hswebframework.web.authorization.basic.web.UserTokenGenerator.TOKEN_TYPE_SESSION_ID;

/**
 * @author zhouhao
 */
public class SimpleSessionIdUserTokenParser implements UserTokenParser {

    @Override
    public ParsedToken parseToken(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        String sessionId;
        if (session == null) {
            //从cookie中获取sessionId
            sessionId = Stream.of(request.getCookies())
                    .filter(cookie -> cookie.getName().equalsIgnoreCase("JSESSIONID"))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        } else {
            sessionId = session.getId();
        }
        if (sessionId != null) {
            return new ParsedToken() {
                @Override
                public String getToken() {
                    return sessionId;
                }

                @Override
                public String getType() {
                    return TOKEN_TYPE_SESSION_ID;
                }
            };
        }
        return null;
    }
}
