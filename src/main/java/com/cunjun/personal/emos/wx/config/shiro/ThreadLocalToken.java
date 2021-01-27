package com.cunjun.personal.emos.wx.config.shiro;

import org.springframework.stereotype.Component;

/**
 * Created by CunjunWang on 2021/1/27.
 */
@Component
public class ThreadLocalToken {

    private final ThreadLocal<String> local = new ThreadLocal<>();

    public void setToken(String token) {
        local.set(token);
    }

    public String getToken() {
        return local.get();
    }

    public void clear() {
        local.remove();
    }

}
