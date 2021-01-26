package com.cunjun.personal.emos.wx.controller;

import com.cunjun.personal.emos.wx.common.util.ResultData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by CunjunWang on 2021/1/26.
 */
@RestController
@RequestMapping("/test")
@Api("测试Web接口")
public class TestController {

    @GetMapping("/sayHello")
    @ApiOperation("最简单的测试方法")
    public ResultData sayHello() {
        return ResultData.ok().put("message", "Hello World!");
    }

}
