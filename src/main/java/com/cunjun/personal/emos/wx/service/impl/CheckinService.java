package com.cunjun.personal.emos.wx.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.cunjun.personal.emos.wx.common.Constant;
import com.cunjun.personal.emos.wx.common.SystemConstants;
import com.cunjun.personal.emos.wx.db.dao.TbCheckinDao;
import com.cunjun.personal.emos.wx.db.dao.TbFaceModelDao;
import com.cunjun.personal.emos.wx.db.dao.TbHolidaysDao;
import com.cunjun.personal.emos.wx.db.dao.TbWorkdayDao;
import com.cunjun.personal.emos.wx.exception.EmosException;
import com.cunjun.personal.emos.wx.service.inf.ICheckinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by CunjunWang on 2021/1/30.
 */
@Slf4j
@Service
@Scope("prototype")
public class CheckinService implements ICheckinService {

    @Value("${emos.face.create-face-model-url}")
    private String createFaceModelUrl;

    @Value("${emos.face.checkin-url}")
    private String checkinUrl;

    @Autowired
    private TbHolidaysDao holidaysDao;

    @Autowired
    private TbWorkdayDao workdayDao;

    @Autowired
    private TbCheckinDao checkinDao;

    @Autowired
    private TbFaceModelDao faceModelDao;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private SystemConstants systemConstants;

    /**
     * 判定是否是合适的签到时间
     */
    @Override
    public String validTimeToCheckin(Integer userId, String date) {
        boolean isHoliday = holidaysDao.selectTodayIsHoliday() != null;
        boolean isWorkday = workdayDao.selectTodayIsWorkday() != null;
        String type = Constant.CHECKIN_DAY_WORKDAY;
        if (DateUtil.date().isWeekend())
            type = Constant.CHECKIN_DAY_HOLIDAY;
        if (isHoliday)
            type = Constant.CHECKIN_DAY_HOLIDAY;
        else if (isWorkday)
            type = Constant.CHECKIN_DAY_WORKDAY;

        if (Constant.CHECKIN_DAY_HOLIDAY.equals(type))
            return "节假日不需要考勤";
        else {
            DateTime now = DateUtil.date();
            String start = DateUtil.today() + " " + systemConstants.attendanceStartTime;
            String end = DateUtil.today() + " " + systemConstants.attendanceEndTime;
            DateTime attendanceStart = DateUtil.parse(start);
            DateTime attendanceEnd = DateUtil.parse(end);

            if (now.isBefore(attendanceStart))
                return "未到上班考勤开始时间";
            else if (now.isAfter(attendanceEnd))
                return "超出了上班考勤结束时间";
            else {
                boolean hasCheckedIn = checkinDao
                        .userHasCheckedInBetween(userId, start, end) != null;
                return hasCheckedIn ? "已经考勤过了" : "可以考勤";
            }
        }
    }

    /**
     * 用户签到
     */
    @Override
    @Transactional
    public void checkin(HashMap<String, Object> param) {
        Date now = DateUtil.date();
        Date attendanceStart = DateUtil.parse(DateUtil.today() + " " + systemConstants.attendanceTime);
        Date attendanceEnd = DateUtil.parse(DateUtil.today() + " " + systemConstants.attendanceEndTime);
        int status = 1;
        if (now.compareTo(attendanceStart) <= 0)
            status = 1;
        else if (now.compareTo(attendanceStart) > 0 && now.compareTo(attendanceEnd) < 0)
            status = 2;

        int userId = (Integer) param.get("userId");
        String faceModel = faceModelDao.selectFaceModelByUserId(userId);
        if (faceModel == null) {
            log.warn("不存在员工[{}]的人脸模型", userId);
            throw new EmosException("不存在人脸模型");
        }
        // TODO
        String path = (String) param.get("path");
        log.info("照片路径: {}", path);
        MultiValueMap<String, Object> request = new LinkedMultiValueMap<>();
        request.add("photo", FileUtil.file(path));
        request.add("targetModel", faceModel);
        String resp = restTemplate.postForObject(checkinUrl, request, String.class);
        log.info("{}", resp);
    }

}
