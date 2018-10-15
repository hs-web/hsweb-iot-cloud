package org.hswebframework.iot.authorization;

import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author zhouhao
 * @since 1.0
 */
@FeignClient(name = "user-server")
public interface UserAuthorizeInfoClient {
    @RequestMapping(value = "/user/authentication/detail",method = RequestMethod.GET)
    ResponseMessage<String> getDetail(@RequestParam("token") String token);
}
