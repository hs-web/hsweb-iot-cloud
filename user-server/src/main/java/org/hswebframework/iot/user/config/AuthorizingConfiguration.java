package org.hswebframework.iot.user.config;

import org.hswebframework.iot.user.controller.UserAuthorizeInfoController;
import org.hswebframework.web.authorization.listener.event.AuthorizingHandleBeforeEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author zhouhao
 * @since 1.0
 */
@Component
public class AuthorizingConfiguration {
    @EventListener
    public void adminUserAllowAllRequest(AuthorizingHandleBeforeEvent event) {
        // 开发环境 admin默认拥有全部权限
        if (event.getContext().getAuthentication().getUser().getUsername().equals("admin")) {
            event.setAllow(true);
            return;
        }
        //通过gateway-server的请求
        if (event.getContext()
                .getAuthentication()
                .getUser()
                .getUsername()
                .equals("gateway-server")
                &&
                event.getContext()
                        .getParamContext()
                        .getTarget() instanceof UserAuthorizeInfoController) {
            event.setAllow(true);
            return;
        }
    }
}
