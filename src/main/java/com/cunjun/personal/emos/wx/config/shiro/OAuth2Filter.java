package com.cunjun.personal.emos.wx.config.shiro;


import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.cunjun.personal.emos.wx.util.JWTUtil;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Created by CunjunWang on 2021/1/27.
 */
@Component
@Scope("prototype") // Spring使用这个类的对象时 是一个多例对象, Spring默认是单例
public class OAuth2Filter extends AuthenticatingFilter {

    @Value("${emos.jwt.cache-expire}")
    private Integer cacheExpire;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ThreadLocalToken threadLocalToken;

    /**
     * 拦截请求以后, 用于把令牌字符串封装成令牌对象
     */
    @Override
    protected AuthenticationToken createToken(ServletRequest request,
                                              ServletResponse response) throws Exception {
        String token = this.getRequestToken((HttpServletRequest) request);
        if (StrUtil.isBlank(token))
            return null;
        return new OAuth2Token(token);
    }

    /**
     * 拦截请求, 判断请求是否要被Shiro处理
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest req = (HttpServletRequest) request;
        return req.getMethod().equals(RequestMethod.OPTIONS.name());
    }

    /**
     * 用于处理所有被拦截下来, 应该被Shiro处理的请求
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        resp.setContentType(ContentType.TEXT_HTML.getMimeType());
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // 允许跨域请求
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));

        threadLocalToken.clear();

        String token = getRequestToken(req);
        if (StrUtil.isBlank(token)) {
            resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
            resp.getWriter().print("无效的令牌");
            return false;
        }

        try {
            jwtUtil.verifyToken(token);
        } catch (TokenExpiredException e) {
            // 查看Redis中的token
            if (redisTemplate.hasKey(token)) {
                // 客户端的令牌过期了, 服务端的没过期
                // 需要刷新令牌
                redisTemplate.delete(token);
                Integer userId = jwtUtil.getUserId(token);
                redisTemplate.opsForValue().set(token, userId + "", cacheExpire, TimeUnit.DAYS);
                threadLocalToken.setToken(token);
            } else {
                // 客户端和服务端的令牌都过期了
                // 需要用户重新登录
                resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
                resp.getWriter().print("令牌已过期");
                return false;
            }
        } catch (JWTDecodeException e) {
            resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
            resp.getWriter().print("无效的令牌");
            return false;
        }

        // 让 Shiro 执行Realm类
        return executeLogin(request, response);
    }

    /**
     * 判定用户登录失败时执行
     */
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e,
                                     ServletRequest request, ServletResponse response) {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setContentType(ContentType.TEXT_HTML.getMimeType());
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
        try {
            resp.getWriter().print(e.getMessage());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }

    /**
     * 提取令牌
     */
    private String getRequestToken(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (StrUtil.isBlank(token))
            token = request.getParameter("token");
        return token;
    }

}
