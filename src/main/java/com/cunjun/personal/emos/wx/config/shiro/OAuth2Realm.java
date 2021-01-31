package com.cunjun.personal.emos.wx.config.shiro;

import com.cunjun.personal.emos.wx.db.pojo.TbUser;
import com.cunjun.personal.emos.wx.service.impl.UserService;
import com.cunjun.personal.emos.wx.util.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Created by CunjunWang on 2021/1/27.
 */
@Slf4j
@Component
public class OAuth2Realm extends AuthorizingRealm {

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof OAuth2Token;
    }

    /**
     * 认证(验证登录时使用)
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token)
            throws AuthenticationException {
        String accessToken = (String) token.getPrincipal();
        Integer userId = jwtUtil.getUserId(accessToken);
        TbUser user = userService.selectValidUserById(userId);
        if (user == null) {
            log.info("用户[{}]账户已被锁定", userId);
            throw new LockedAccountException("账号已被锁定, 请联系管理员");
        }
        return new SimpleAuthenticationInfo(user, accessToken, getName());
    }

    /**
     * 授权方法(登录后鉴权时调用)
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection collection) {
        TbUser user = (TbUser) collection.getPrimaryPrincipal();
        Integer userId = user.getId();
        Set<String> permissions = userService.searchUserPermissions(userId);
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.setStringPermissions(permissions);
        return info;
    }

}
