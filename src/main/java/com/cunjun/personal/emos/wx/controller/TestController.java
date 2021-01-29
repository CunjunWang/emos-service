package com.cunjun.personal.emos.wx.controller;

import com.cunjun.personal.emos.wx.common.ResultData;
import com.cunjun.personal.emos.wx.controller.form.TestSayHelloForm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Created by CunjunWang on 2021/1/26.
 */
@RestController
@RequestMapping("/test")
@Api(value = "测试Web接口", description = "测试Web接口")
public class TestController {

    @PostMapping("/sayHello")
    @ApiOperation(value = "最简单的测试方法")
    public ResultData sayHello(@Valid @RequestBody TestSayHelloForm form) {
        return ResultData.ok().put("message", "Hello, " + form.getName());
    }

}
