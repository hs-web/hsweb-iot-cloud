package org.hswebframework.iot.user.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.AuthenticationHolder;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.token.UserToken;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.logging.AccessLogger;
import org.hswebframework.web.organizational.authorization.PersonnelAuthenticationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhouhao
 * @since 1.0
 */
@RestController
@RequestMapping("/user/authentication")
@Authorize(permission = "user-token")
@Api(hidden = true)
public class UserAuthorizeInfoController {

    @Autowired
    PersonnelAuthenticationManager authorizationManager;

    @Autowired
    UserTokenManager userTokenManager;

    //提供给网关服务的接口
    @GetMapping("/detail")
    @AccessLogger(value = "获取用户和人员权限信息", ignore = true)
    @ApiOperation(value = "获取用户和人员权限信息", hidden = true)
    @Authorize(action = Permission.ACTION_GET)
    public ResponseMessage<UserAuthorizeInfo> getUserAuthorizeInfo(@RequestParam String token) {
        UserToken userToken = userTokenManager.getByToken(token);
        if (userToken != null && userToken.isNormal()) {
            userTokenManager.touch(token);
            UserAuthorizeInfo info = new UserAuthorizeInfo();
            info.setUser(AuthenticationHolder.get(userToken.getUserId()));
            info.setPersonnel(authorizationManager.getPersonnelAuthorizationByUserId(userToken.getUserId()));
            return ResponseMessage.ok(info);
        }
        return ResponseMessage.ok();
    }

    @GetMapping("/detail/me")
    @Authorize(merge = false)
    @AccessLogger(value = "获取用户和人员权限信息", ignore = true)
    @ApiOperation(value = "获取用户和人员权限信息", hidden = true)
    public ResponseMessage<UserAuthorizeInfo> getUserAuthorizeInfo(Authentication authentication) {
        UserAuthorizeInfo info = new UserAuthorizeInfo();
        info.setUser(authentication);
        info.setPersonnel(authorizationManager.getPersonnelAuthorizationByUserId(authentication.getUser().getId()));
        return ResponseMessage.ok(info);
    }


}
