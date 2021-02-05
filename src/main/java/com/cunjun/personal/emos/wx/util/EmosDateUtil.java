package com.cunjun.personal.emos.wx.util;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.cunjun.personal.emos.wx.common.Constant;
import com.cunjun.personal.emos.wx.common.SystemConstants;
import com.cunjun.personal.emos.wx.controller.form.checkin.SearchMonthlyCheckinForm;
import com.cunjun.personal.emos.wx.exception.EmosException;
import com.cunjun.personal.emos.wx.service.impl.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

/**
 * Created by CunjunWang on 2021/2/3.
 */
@Slf4j
@Component
public class EmosDateUtil {

    @Autowired
    private SystemConstants systemConstants;

    @Autowired
    private UserService userService;

    /**
     * 构建日期变量
     */
    public HashMap<String, DateTime> buildDates() {
        HashMap<String, DateTime> result = new HashMap<>();

        DateTime now = DateUtil.date();
        DateTime attendanceStart = DateUtil.parse(DateUtil.today() + " " + systemConstants.attendanceStartTime);
        DateTime attendanceEnd = DateUtil.parse(DateUtil.today() + " " + systemConstants.attendanceEndTime);
        DateTime closingStart = DateUtil.parse(DateUtil.today() + " " + systemConstants.closingStartTime);
        DateTime closingEnd = DateUtil.parse(DateUtil.today() + " " + systemConstants.closingEndTime);

        result.put(Constant.DATETIME_NOW, now);
        result.put(Constant.DATETIME_ATTENDANCE_START, attendanceStart);
        result.put(Constant.DATETIME_ATTENDANCE_END, attendanceEnd);
        result.put(Constant.DATETIME_CLOSING_START, closingStart);
        result.put(Constant.DATETIME_CLOSING_END, closingEnd);

        return result;
    }

    /**
     * 与入职日期做比较, 获取用户考勤开始日期
     */
    public DateTime getCheckinStartDate(Integer userId) {
        DateTime hireDate = DateUtil.parse(userService.searchUserHireDate(userId));
        DateTime startDate = DateUtil.beginOfWeek(DateUtil.date());
        if (startDate.isBefore(hireDate))
            startDate = hireDate;
        return startDate;
    }

    /**
     * 获取查询月考勤记录的起始时间
     */
    public DateTime getMonthlyCheckinStatsStartDate(Integer userId, SearchMonthlyCheckinForm form) {
        DateTime hireDate = DateUtil.parse(userService.searchUserHireDate(userId));
        String month = form.getMonth() < 10 ? "0" + form.getMonth() : form.getMonth() + "";
        String year = form.getYear() + "";
        DateTime startDate = DateUtil.parse(year + "-" + month + "-01");
        if (startDate.isBefore(DateUtil.beginOfMonth(hireDate)))
            throw new EmosException("只能查询入职之后的考勤数据");
        if (startDate.isBefore(hireDate))
            startDate = hireDate;
        return startDate;
    }

    /**
     * 统计签到数据
     */
    public HashMap<String, Integer> countCheckin(List<HashMap<String, String>> list) {
        int sumOnTime = 0, sumLate = 0, sumAbsence = 0;
        for (HashMap<String, String> d : list) {
            String type = d.get("type");
            String status = d.get("status");
            if (type.equals(Constant.CHECKIN_DAY_WORKDAY))
                if (Constant.CHECKIN_STATUS_OK.equals(status))
                    sumOnTime++;
                else if (Constant.CHECKIN_STATUS_LATE.equals(status))
                    sumLate++;
                else if (Constant.CHECKIN_STATUS_ABSENCE.equals(status))
                    sumAbsence++;
        }
        HashMap<String, Integer> res = new HashMap<>();
        res.put("sumOnTime", sumOnTime);
        res.put("sumLate", sumLate);
        res.put("sumAbsence", sumAbsence);
        return res;
    }

}
