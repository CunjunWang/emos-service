package com.cunjun.personal.emos.wx.controller;

import cn.hutool.core.date.DateUtil;
import com.cunjun.personal.emos.wx.common.ResultData;
import com.cunjun.personal.emos.wx.service.impl.CheckinService;
import com.cunjun.personal.emos.wx.util.JWTUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by CunjunWang on 2021/1/30.
 */
@Slf4j
@RestController
@RequestMapping("/checkin")
@Api(value = "用户签到Web接口", description = "用户签到Web接口")
public class CheckinController {

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private CheckinService checkinService;

    @RequestMapping(value = "/ableOrNot", method = RequestMethod.GET)
    @ApiOperation(value = "判断用户是否可以签到")
    public ResultData ableToCheckinOrNot(
            @RequestHeader("token") String token) {
        Integer userId = jwtUtil.getUserId(token);
        String today = DateUtil.today();
        String result = checkinService.validTimeToCheckin(userId, today);
        return ResultData.ok(result);
    }

}
