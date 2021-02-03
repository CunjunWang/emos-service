package com.cunjun.personal.emos.wx.util;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.cunjun.personal.emos.wx.common.Constant;
import com.cunjun.personal.emos.wx.common.SystemConstants;
import com.cunjun.personal.emos.wx.service.impl.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

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
}
