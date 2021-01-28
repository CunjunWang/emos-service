package com.cunjun.personal.emos.wx.aop;

import com.cunjun.personal.emos.wx.common.ResultData;
import com.cunjun.personal.emos.wx.config.shiro.ThreadLocalToken;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by CunjunWang on 2021/1/27.
 */
@Aspect
@Component
public class TokenAspect {

    @Autowired
    private ThreadLocalToken threadLocalToken;

    @Pointcut("execution(public * com.cunjun.personal.emos.wx.controller.*.*(..))")
    public void aspect() {

    }

    @Around("aspect()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        ResultData r = (ResultData) point.proceed(); // 方法执行结果
        String token = threadLocalToken.getToken();
        // 如果ThreadLocal中有新的令牌, 把他更新到客户端
        if (token != null) {
            r.put("token", token);
            threadLocalToken.clear();
        }
        return r;
    }

}
