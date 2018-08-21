package org.hswebframework.iot.user.controller;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.organizational.authorization.PersonnelAuthentication;

@Getter
@Setter
public class UserAuthorizeInfo {
    private Authentication user;

    private PersonnelAuthentication personnel;

}