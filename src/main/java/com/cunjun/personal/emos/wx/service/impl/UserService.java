package com.cunjun.personal.emos.wx.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cunjun.personal.emos.wx.common.Constant;
import com.cunjun.personal.emos.wx.db.dao.TbUserDao;
import com.cunjun.personal.emos.wx.db.pojo.TbUser;
import com.cunjun.personal.emos.wx.exception.EmosException;
import com.cunjun.personal.emos.wx.service.inf.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.Set;

/**
 * Created by CunjunWang on 2021/1/28.
 */
@Slf4j
@Service
@Scope("prototype")
@PropertySource(value = {"classpath:secret.properties", "classpath:application.yml"})
public class UserService implements IUserService {

    @Value("${emos.wx.open-id-url}")
    private String wxOpenIdUrl;

    @Value("${nixie.emos.wx.app-id}")
    private String wxAppId;

    @Value("${nixie.emos.wx.app-secret}")
    private String wxAppSecret;

    @Autowired
    private TbUserDao userDao;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 注册新用户
     */
    @Override
    @Transactional
    public Integer registerUser(String wxCode, String registrationCode, String nickname, String avatarUrl) {
        if (Constant.ROOT_REGISTRATION_CODE.equals(registrationCode)) {
            boolean hasRootUser = userDao.hasRootUser();
            if (!hasRootUser) {
                TbUser rootManager = new TbUser();
                String openId = getOpenId(wxCode);
                rootManager.setNickname(nickname);
                rootManager.setOpenId(openId);
                rootManager.setPhoto(avatarUrl);
                rootManager.setRole("[0]");
                rootManager.setRoot(true);
                rootManager.setStatus((byte) 1);
                rootManager.setCreateTime(new Date());
                userDao.insertSelective(rootManager);
                return userDao.searchIdByOpenId(openId);
            } else {
                log.error("超级管理员已存在, 无法绑定账号");
                throw new EmosException("无法绑定超级管理员账号");
            }
        } else {
            // 普通员工注册
        }
        return 0;
    }

    /**
     * 根据用户Id查询权限列表
     */
    @Override
    public Set<String> searchUserPermissions(Integer userId) {
        log.info("根据用户Id查询用户[{}]的权限列表", userId);
        return userDao.searchUserPermissions(userId);
    }

    /**
     * 用户登录
     */
    @Override
    public Integer login(String wxCode) {
        String openId = getOpenId(wxCode);
        Integer userId = userDao.searchIdByOpenId(openId);
        if (userId == null) {
            log.error("用户[{}]账户不存在", userId);
            throw new EmosException("用户账户不存在");
        }
        log.info("用户[{}]执行登录", userId);
        // TODO: 从消息队列中接收消息转移到消息表
        return userId;
    }

    /**
     * 根据主键查询用户
     */
    @Override
    public TbUser selectValidUserById(Integer userId) {
        log.info("根据主键[{}]查询用户", userId);
        return userDao.selectValidUserById(userId);
    }

    /**
     * 查询用户入职日期
     */
    @Override
    public String searchUserHireDate(Integer userId) {
        log.info("查询用户[{}]的入职日期", userId);
        return userDao.searchUserHireDate(userId);
    }

    /**
     * 获取用户微信OpenID
     */
    private String getOpenId(String code) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("appid", wxAppId);
        map.add("secret", wxAppSecret);
        map.add("js_code", code);
        map.add("grant_type", "authorization_code");
        String obj = restTemplate.postForObject(wxOpenIdUrl, map, String.class);
        JSONObject res = JSON.parseObject(obj);
        if (res.getString("errmsg") != null) {
            String msg = res.getString("errmsg");
            log.error("获取用户微信OpenId失败, 失败原因[{}]", msg);
            throw new EmosException(msg);
        }
        String openId = res.getString("openid");
        if (openId == null || StringUtils.isEmpty(openId))
            throw new RuntimeException("临时登录凭证错误");
        return openId;
    }

}
