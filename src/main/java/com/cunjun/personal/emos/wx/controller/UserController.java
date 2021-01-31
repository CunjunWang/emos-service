package com.cunjun.personal.emos.wx.controller;

import com.cunjun.personal.emos.wx.common.Constant;
import com.cunjun.personal.emos.wx.common.ResultData;
import com.cunjun.personal.emos.wx.controller.form.user.LoginForm;
import com.cunjun.personal.emos.wx.controller.form.user.RegisterForm;
import com.cunjun.personal.emos.wx.service.impl.UserService;
import com.cunjun.personal.emos.wx.util.JWTUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Set;

/**
 * Created by CunjunWang on 2021/1/29.
 */
@RestController
@RequestMapping("/user")
@Api(value = "用户模块Web接口", description = "用户模块Web接口")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JWTUtil jwtUtil;

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @ApiOperation(value = "用户注册")
    public ResultData register(@Valid @RequestBody RegisterForm form) {
        Integer id = userService.registerUser(form.getWxCode(), form.getRegistrationCode(),
                form.getNickname(), form.getAvatarUrl());
        String token = jwtUtil.createToken(id);
        Set<String> permissionsSet = userService.searchUserPermissions(id);
        jwtUtil.cacheToken(token, id);
        return ResultData.ok("用户注册成功")
                .put(Constant.RETURN_VALUE_TOKEN, token)
                .put(Constant.RETURN_VALUE_PERMISSIONS, permissionsSet);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ApiOperation(value = "用户登录")
    public ResultData register(@Valid @RequestBody LoginForm form) {
        Integer id = userService.login(form.getWxCode());
        String token = jwtUtil.createToken(id);
        Set<String> permissionsSet = userService.searchUserPermissions(id);
        jwtUtil.cacheToken(token, id);
        return ResultData.ok("用户登录成功")
                .put(Constant.RETURN_VALUE_TOKEN, token)
                .put(Constant.RETURN_VALUE_PERMISSIONS, permissionsSet);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ApiOperation(value = "添加用户")
    @RequiresPermissions(value = {"ROOT", "USER:ADD"}, logical = Logical.OR)
    public ResultData addUser() {
        return ResultData.ok();
    }

}
