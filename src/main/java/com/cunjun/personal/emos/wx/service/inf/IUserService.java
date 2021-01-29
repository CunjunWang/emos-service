package com.cunjun.personal.emos.wx.service.inf;

import java.util.Set;

/**
 * Created by CunjunWang on 2021/1/28.
 */
public interface IUserService {

    /**
     * 注册新用户
     */
    Integer registerUser(String wxCode, String registrationCode, String nickname, String avatarUrl);

    /**
     * 根据用户Id查询权限列表
     */
    Set<String> searchUserPermissions(Integer userId);

    /**
     * 用户登录
     */
    Integer login(String wxCode);

}
