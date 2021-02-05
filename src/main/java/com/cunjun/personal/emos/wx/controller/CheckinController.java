package com.cunjun.personal.emos.wx.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.cunjun.personal.emos.wx.common.ResultData;
import com.cunjun.personal.emos.wx.common.SystemConstants;
import com.cunjun.personal.emos.wx.controller.form.checkin.CheckinForm;
import com.cunjun.personal.emos.wx.controller.form.checkin.SearchMonthlyCheckinForm;
import com.cunjun.personal.emos.wx.exception.EmosException;
import com.cunjun.personal.emos.wx.service.impl.CheckinService;
import com.cunjun.personal.emos.wx.service.impl.UserService;
import com.cunjun.personal.emos.wx.util.EmosDateUtil;
import com.cunjun.personal.emos.wx.util.JWTUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

/**
 * Created by CunjunWang on 2021/1/30.
 */
@Slf4j
@RestController
@RequestMapping("/checkin")
@Api(value = "用户签到Web接口", description = "用户签到Web接口")
public class CheckinController {

    @Value("${emos.temp-image-folder}")
    private String tempImageFolder;

    @Autowired
    private SystemConstants systemConstants;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private EmosDateUtil emosDateUtil;

    @Autowired
    private CheckinService checkinService;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/ableOrNot", method = RequestMethod.GET)
    @ApiOperation(value = "判断用户是否可以签到")
    public ResultData ableToCheckinOrNot(
            @RequestHeader("token") String token) {
        Integer userId = jwtUtil.getUserId(token);
        String today = DateUtil.today();
        String result = checkinService.validTimeToCheckin(userId, today);
        return ResultData.ok(result);
    }

    @RequestMapping(value = "/checkin", method = RequestMethod.POST)
    @ApiOperation(value = "用户签到")
    public ResultData checkin(
            @Valid CheckinForm form,
            @RequestParam(name = "photo") MultipartFile file,
            @RequestHeader("token") String token) {
        if (file == null)
            return ResultData.error("没有上传文件");
        Integer userId = jwtUtil.getUserId(token);
        String filename = file.getOriginalFilename().toLowerCase();
        if (!filename.endsWith(".jpg"))
            return ResultData.error("必须提交JPG格式图片");
        String tempPath = tempImageFolder + "/" + filename;
        log.info("checkin提交的图片路径: {}", tempPath);
        try {
            file.transferTo(Paths.get(tempPath));
            HashMap<String, Object> param = new HashMap<>();
            param.put("userId", userId);
            param.put("path", tempPath);
            param.put("district", form.getDistrict());
            param.put("city", form.getCity());
            param.put("province", form.getProvince());
            param.put("country", form.getCountry());
            param.put("address", form.getAddress());
            checkinService.checkin(param);
            return ResultData.ok("签到成功");
        } catch (IOException ioe) {
            log.error(ioe.getMessage(), ioe);
            throw new EmosException("图片保存错误");
        } finally {
            // 删除上传的临时自拍
            FileUtil.del(tempPath);
        }
    }

    @RequestMapping(value = "/faceModel", method = RequestMethod.POST)
    @ApiOperation(value = "创建人脸模型")
    public ResultData createFaceModel(
            @RequestParam(name = "photo") MultipartFile file,
            @RequestHeader("token") String token) {
        if (file == null)
            return ResultData.error("没有上传文件");
        Integer userId = jwtUtil.getUserId(token);
        String filename = file.getOriginalFilename().toLowerCase();
        if (!filename.endsWith(".jpg"))
            return ResultData.error("必须提交JPG格式图片");
        String tempPath = tempImageFolder + "/" + filename;
        log.info("createFaceModel提交的图片路径: {}", tempPath);
        try {
            file.transferTo(Paths.get(tempPath));
            checkinService.createFaceModel(userId, tempPath);
            return ResultData.ok("人脸建模成功");
        } catch (IOException ioe) {
            log.error(ioe.getMessage(), ioe);
            throw new EmosException("图片保存错误");
        } finally {
            // 删除上传的临时自拍
            FileUtil.del(tempPath);
        }
    }

    @RequestMapping(value = "/today", method = RequestMethod.GET)
    @ApiOperation(value = "查询用户当日签到数据")
    public ResultData searchTodayCheckin(@RequestHeader("token") String token) {
        Integer userId = jwtUtil.getUserId(token);

        HashMap<String, Object> checkin = checkinService.searchUserTodayCheckin(userId);

        checkin.put("attendanceTime", systemConstants.attendanceTime);
        checkin.put("closingTime", systemConstants.closingTime);
        Long days = checkinService.searchUserTotalCheckinDays(userId);
        checkin.put("checkinDays", days);

        DateTime startDate = emosDateUtil.getCheckinStartDate(userId);
        DateTime endDate = DateUtil.endOfWeek(DateUtil.date());
        HashMap<String, Object> param = new HashMap<>();
        param.put("startDate", startDate.toString());
        param.put("endDate", endDate.toString());
        param.put("userId", userId);
        List<HashMap<String, String>> list = checkinService.searchUserWeeklyCheckinResult(param);
        checkin.put("weeklyCheckin", list);

        return ResultData.ok().put("result", checkin);
    }

    @RequestMapping(value = "/record/searchMonthly", method = RequestMethod.POST)
    @ApiOperation(value = "查询用户一个月签到数据")
    public ResultData searchMonthlyCheckin(
            @Valid @RequestBody SearchMonthlyCheckinForm form,
            @RequestHeader("token") String token) {
        Integer userId = jwtUtil.getUserId(token);

        DateTime startDate = emosDateUtil.getMonthlyCheckinStatsStartDate(userId, form);
        DateTime endDate = DateUtil.endOfMonth(startDate);
        HashMap<String, Object> param = new HashMap<>();
        param.put("userId", userId);
        param.put("startDate", startDate.toString());
        param.put("endDate", endDate.toString());
        List<HashMap<String, String>> list = checkinService.searchUserMonthlyCheckinResult(param);
        HashMap<String, Integer> counts = emosDateUtil.countCheckin(list);

        return ResultData.ok("查询月考勤信息成功")
                .put("list", list)
                .put("counts", counts);
    }

}
