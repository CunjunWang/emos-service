package com.cunjun.personal.emos.wx.config.shiro;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by CunjunWang on 2021/1/27.
 */
@Slf4j
@Component
public class JWTUtil {

    @Value("${emos.jwt.secret}")
    private String secret;

    @Value("${emos.jwt.expire}")
    private int expire;

    /**
     * 根据用户 ID 创建 JWT 令牌
     */
    public String createToken(Integer userId) {
        Date expireDate = DateUtil.offset(new Date(), DateField.DAY_OF_YEAR, expire);
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTCreator.Builder builder = JWT.create();
        return builder
                .withClaim("userId", userId)
                .withExpiresAt(expireDate)
                .sign(algorithm);
    }

    /**
     * 从 JWT 令牌中获取用户 ID
     */
    public int getUserId(String token) {
        DecodedJWT decodedJWT = JWT.decode(token);
        Claim userId = decodedJWT.getClaim("userId");
        return userId.asInt();
    }

    /**
     * 验证令牌有效性
     */
    public void verifyToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier = JWT.require(algorithm).build();
        verifier.verify(token);
    }
}
