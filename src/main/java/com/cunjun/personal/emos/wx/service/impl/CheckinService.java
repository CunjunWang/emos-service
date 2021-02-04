package com.cunjun.personal.emos.wx.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateRange;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.cunjun.personal.emos.wx.common.Constant;
import com.cunjun.personal.emos.wx.common.SystemConstants;
import com.cunjun.personal.emos.wx.db.dao.*;
import com.cunjun.personal.emos.wx.db.pojo.TbCheckin;
import com.cunjun.personal.emos.wx.db.pojo.TbFaceModel;
import com.cunjun.personal.emos.wx.exception.EmosException;
import com.cunjun.personal.emos.wx.service.inf.ICheckinService;
import com.cunjun.personal.emos.wx.util.EmailUtil;
import com.cunjun.personal.emos.wx.util.EmosDateUtil;
import com.cunjun.personal.emos.wx.util.JSoupUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by CunjunWang on 2021/1/30.
 */
@Slf4j
@Service
@Scope("prototype")
public class CheckinService implements ICheckinService {

    @Value("${emos.checkPandemic}")
    private Boolean checkPandemic;

    @Value("${emos.pandemic-risk-url}")
    private String pandemicRiskUrl;

    @Autowired
    private TbCityDao cityDao;

    @Autowired
    private TbWorkdayDao workdayDao;

    @Autowired
    private TbCheckinDao checkinDao;

    @Autowired
    private TbHolidaysDao holidaysDao;

    @Autowired
    private TbFaceModelDao faceModelDao;

    @Autowired
    private EmailUtil emailUtil;

    @Autowired
    private JSoupUtil jSoupUtil;

    @Autowired
    private EmosDateUtil emosDateUtil;

    @Autowired
    private SystemConstants systemConstants;

    @Autowired
    private FaceModelService faceModelService;

    /**
     * 判定是否是合适的签到时间
     */
    @Override
    public String validTimeToCheckin(Integer userId, String date) {
        // 判断今天是否是工作日
        boolean isHoliday = holidaysDao.selectTodayIsHoliday() != null;
        boolean isWorkday = workdayDao.selectTodayIsWorkday() != null;
        String type = Constant.CHECKIN_DAY_WORKDAY;
        if (DateUtil.date().isWeekend())
            type = Constant.CHECKIN_DAY_HOLIDAY;
        if (isHoliday)
            type = Constant.CHECKIN_DAY_HOLIDAY;
        else if (isWorkday)
            type = Constant.CHECKIN_DAY_WORKDAY;

        if (Constant.CHECKIN_DAY_HOLIDAY.equals(type)) // 休息日不需要考勤
            return Constant.CHECKIN_NO_NEED_FOR_HOLIDAY;
        else {
            HashMap<String, DateTime> dates = emosDateUtil.buildDates();
            dates.get(Constant.DATETIME_ATTENDANCE_START);
            // 尚未到上班考勤时间
            if (dates.get(Constant.DATETIME_NOW).isBefore(dates.get(Constant.DATETIME_ATTENDANCE_START)))
                return Constant.CHECKIN_TOO_EARLY_FOR_ATTENDANCE;
            // 已经过了上班考勤时间
            if (dates.get(Constant.DATETIME_NOW).isAfter(dates.get(Constant.DATETIME_ATTENDANCE_END)))
                return Constant.CHECKIN_TOO_LATE_FOR_ATTENDANCE;
            // 若当天已经考勤过了不可重复考勤
            String start = DateUtil.today() + " " + systemConstants.attendanceStartTime;
            String end = DateUtil.today() + " " + systemConstants.attendanceEndTime;
            boolean hasCheckedIn = checkinDao.userHasCheckedInBetween(userId, start, end) != null;
            return hasCheckedIn ? Constant.CHECKIN_DUPLICATE_OPERATION : Constant.CHECKIN_OK_TO_CHECKIN;
        }
    }

    /**
     * 用户签到考勤
     */
    @Override
    @Transactional
    public void checkin(HashMap<String, Object> param) {
        Integer userId = (Integer) param.get("userId");
        String today = DateUtil.today();
        // 考勤过了不需要重复考勤
        Integer checkinRecord = checkinDao.selectByUserAndDate(userId, today);
        if (checkinRecord > 0)
            throw new EmosException(Constant.CHECKIN_DUPLICATE_OPERATION);

        HashMap<String, DateTime> dates = emosDateUtil.buildDates();
        Date now = dates.get(Constant.DATETIME_NOW);
        Date attendanceStart = dates.get(Constant.DATETIME_ATTENDANCE_START);
        Date attendanceEnd = dates.get(Constant.DATETIME_ATTENDANCE_END);
        // 考勤状态, 是否迟到
        int status = Constant.CHECKIN_DB_STATUS_OK;
        if (now.compareTo(attendanceStart) > 0 && now.compareTo(attendanceEnd) < 0)
            status = Constant.CHECKIN_DB_STATUS_LATE;

        String faceModel = faceModelDao.selectFaceModelByUserId(userId);
        if (faceModel == null) {
            log.warn("不存在员工[{}]的人脸模型", userId);
            throw new EmosException(Constant.FACE_MODEL_NO_CORRESPONDING_MODEL);
        }

        String recognizeResult = faceModelService.checkin((String) param.get("path"), faceModel);
        if (Constant.FACE_MODEL_RESULT_TRUE.equals(recognizeResult)) {
            TbCheckin record = new TbCheckin();
            record.setUserId(userId);
            record.setAddress((String) param.get("address"));
            record.setDistrict((String) param.get("district"));
            record.setCity((String) param.get("city"));
            record.setCountry((String) param.get("country"));
            record.setProvince((String) param.get("province"));
            record.setStatus((byte) status);
            record.setDate(DateUtil.today());
            record.setCreateTime(now);
            if (checkPandemic) { // 是否需要检查新冠疫情等级
                String risk = this.getCovidPandemicRisk((String) param.get("city"), (String) param.get("district"));
                sendPandemicWarningEmail(param, risk);
                switch (risk) {
                    case Constant.CHECKIN_LOCATION_PANDEMIC_MID_RISK -> record.setRisk(Constant.CHECKIN_LOCATION_PANDEMIC_DB_MID_RISK);
                    case Constant.CHECKIN_LOCATION_PANDEMIC_HIGH_RISK -> record.setRisk(Constant.CHECKIN_LOCATION_PANDEMIC_DB_HIGH_RISK);
                    default -> record.setRisk(Constant.CHECKIN_LOCATION_PANDEMIC_DB_LOW_RISK);
                }
            }
            // 入库考勤记录
            checkinDao.insertSelective(record);
        } else
            throw new EmosException(Constant.FACE_MODEL_SERVICE_ERROR);
    }

    /**
     * 创建人脸识别模型
     */
    @Override
    @Transactional
    public void createFaceModel(Integer userId, String photoPath) {
        log.info("创建用户[{}]人脸识别模型", userId);
        String faceModel = faceModelService.createFaceModel(photoPath);
        TbFaceModel model = new TbFaceModel();
        model.setUserId(userId);
        model.setFaceModel(faceModel);
        faceModelDao.insertSelective(model);
    }

    /**
     * 查询用户当天签到状态
     */
    @Override
    public HashMap<String, Object> searchUserTodayCheckin(Integer userId) {
        log.info("查询用户[{}]当天[{}]签到状态", userId, DateUtil.today());
        return checkinDao.selectTodayCheckinByUserId(userId);
    }

    /**
     * 查询用户总签到天数
     */
    @Override
    public Long searchUserTotalCheckinDays(Integer userId) {
        log.info("查询用户[{}]总签到天数", userId);
        return checkinDao.selectTotalCheckinDaysByUserId(userId);
    }

    /**
     * 查询用户一周内的签到记录
     */
    @Override
    public List<HashMap<String, String>> searchUserWeeklyCheckinResult(HashMap<String, Object> param) {
        Integer userId = (Integer) param.get("userId");
        String start = (String) param.get("startDate");
        String end = (String) param.get("endDate");
        log.info("查询用户[{}]一周内的签到记录", userId);
        List<HashMap<String, String>> weeklyCheckin = checkinDao.selectWeeklyCheckinByUserId(userId, start, end);
        List<String> holidays = holidaysDao.searchHolidayInRange(start, end);
        List<String> workdays = workdayDao.searchWorkdayInRange(start, end);
        DateTime startDate = DateUtil.parseDate(start);
        DateTime endDate = DateUtil.parseDate(end);
        DateRange range = DateUtil.range(startDate, endDate, DateField.DAY_OF_MONTH);

        List<HashMap<String, String>> result = new ArrayList<>();
        range.forEach(d -> {
            String date = d.toString("yyyy-MM-dd");
            String type = this.getDayType(d, date, holidays, workdays);
            String status = this.getCheckinStatus(d, date, type, weeklyCheckin);
            HashMap<String, String> map = new HashMap<>();
            map.put("date", date);
            map.put("status", status);
            map.put("type", type);
            map.put("day", d.dayOfWeekEnum().toChinese("周"));
            result.add(map);
        });
        return result;
    }

    /**
     * 发送疫情告警邮件
     */
    private void sendPandemicWarningEmail(HashMap<String, Object> param, String risk) {
        Integer userId = (Integer) param.get("userId");
        String address = (String) param.get("address");
        if (Constant.CHECKIN_LOCATION_PANDEMIC_HIGH_RISK.equals(risk))
            emailUtil.sendMessage(userId, address);
    }

    /**
     * 解析新冠疫情风险
     */
    private String getCovidPandemicRisk(String city, String district) {
        log.info("查询[{}][{}]的疫情风险等级", city, district);
        String risk = Constant.CHECKIN_LOCATION_PANDEMIC_LOW_RISK;
        if (!StringUtils.isEmpty(city) && !StringUtils.isEmpty(district)) {
            String code = cityDao.searchCodeByCity(city);
            String url = String.format(pandemicRiskUrl, code, district);
            Element doc = jSoupUtil.getDocument(url);
            Elements elements = doc.getElementsByClass("list-content");
            if (elements.size() > 0) {
                Element e = elements.get(0);
                risk = e.select("p:last-child").text();
            }
        }
        return risk;
    }

    /**
     * 获取日期类型
     */
    private String getDayType(DateTime d, String date,
                              List<String> holidays, List<String> workdays) {
        String type = Constant.CHECKIN_DAY_WORKDAY;

        if (d.isWeekend())
            type = Constant.CHECKIN_DAY_HOLIDAY;
        if (holidays != null && holidays.contains(date))
            type = Constant.CHECKIN_DAY_HOLIDAY;
        else if (workdays != null && workdays.contains(date))
            type = Constant.CHECKIN_DAY_WORKDAY;

        return type;
    }

    /**
     * 获取考勤状态
     */
    private String getCheckinStatus(DateTime d, String date, String type,
                                    List<HashMap<String, String>> weeklyCheckin) {
        String status = "";
        if (type.equals(Constant.CHECKIN_DAY_WORKDAY) && DateUtil.compare(d, DateUtil.date()) <= 0) { // 该日期已经到或者过去了, 查看考勤状态
            status = Constant.CHECKIN_STATUS_ABSENCE;
            for (HashMap<String, String> map : weeklyCheckin)
                if (map.containsValue(date)) {
                    status = map.get("status");
                    break;
                }
            // 若当天考勤还没结束并且该员工还没考勤, 不可以算作旷工
            DateTime endTime = emosDateUtil.buildDates().get(Constant.DATETIME_ATTENDANCE_END);
            String today = DateUtil.today();
            if (date.equals(today) && DateUtil.date().isBefore(endTime)
                    && !StringUtils.isEmpty(status))
                status = "";
        }
        return status;
    }

}
