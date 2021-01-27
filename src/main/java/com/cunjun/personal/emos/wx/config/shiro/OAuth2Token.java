package com.cunjun.personal.emos.wx.config.shiro;

import lombok.Data;
import org.apache.shiro.authc.AuthenticationToken;

/**
 * Created by CunjunWang on 2021/1/27.
 */
@Data
public class OAuth2Token implements AuthenticationToken {

    private String token;

    public OAuth2Token(String token) {
        this.token = token;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}
