package com.cunjun.personal.emos.wx.service.inf;

import com.cunjun.personal.emos.wx.db.pojo.TbUser;

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

    /**
     * 根据主键查询用户
     */
    TbUser selectValidUserById(Integer userId);

    /**
     * 查询用户入职日期
     */
    String searchUserHireDate(Integer userId);
}
