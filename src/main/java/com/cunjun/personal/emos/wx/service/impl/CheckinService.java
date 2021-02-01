package com.cunjun.personal.emos.wx.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.cunjun.personal.emos.wx.common.Constant;
import com.cunjun.personal.emos.wx.common.SystemConstants;
import com.cunjun.personal.emos.wx.db.dao.*;
import com.cunjun.personal.emos.wx.db.pojo.TbCheckin;
import com.cunjun.personal.emos.wx.db.pojo.TbFaceModel;
import com.cunjun.personal.emos.wx.exception.EmosException;
import com.cunjun.personal.emos.wx.service.inf.ICheckinService;
import com.cunjun.personal.emos.wx.util.EmailUtil;
import com.cunjun.personal.emos.wx.util.JSoupUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by CunjunWang on 2021/1/30.
 */
@Slf4j
@Service
@Scope("prototype")
@PropertySource(value = {"classpath:application.yml", "classpath:secret.properties"})
public class CheckinService implements ICheckinService {

    @Value("${emos.face.create-face-model-url}")
    private String createFaceModelUrl;

    @Value("${emos.face.checkin-url}")
    private String checkinUrl;

    @Value("${course.api-code}")
    private String apiCode;

    @Value("${emos.checkPandemic}")
    private Boolean checkPandemic;

    @Value("${emos.pandemic-risk-url}")
    private String pandemicRiskUrl;

    @Autowired
    private TbHolidaysDao holidaysDao;

    @Autowired
    private TbWorkdayDao workdayDao;

    @Autowired
    private TbCheckinDao checkinDao;

    @Autowired
    private TbCityDao cityDao;

    @Autowired
    private TbFaceModelDao faceModelDao;

    @Autowired
    private EmailUtil emailUtil;

    @Autowired
    private JSoupUtil jSoupUtil;

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
        Integer userId = (Integer) param.get("userId");
        String today = DateUtil.today();
        Integer checkinRecord = checkinDao.selectByUserAndDate(userId, today);
        if (checkinRecord > 0)
            throw new EmosException("您今天已经签到过了");

        Date now = DateUtil.date();
        Date attendanceStart = DateUtil.parse(DateUtil.today() + " " + systemConstants.attendanceTime);
        Date attendanceEnd = DateUtil.parse(DateUtil.today() + " " + systemConstants.attendanceEndTime);
        int status = 1;
        if (now.compareTo(attendanceStart) <= 0)
            status = 1;
        else if (now.compareTo(attendanceStart) > 0 && now.compareTo(attendanceEnd) < 0)
            status = 2;

        String faceModel = faceModelDao.selectFaceModelByUserId(userId);
        if (faceModel == null) {
            log.warn("不存在员工[{}]的人脸模型", userId);
            throw new EmosException("不存在人脸模型");
        }
        String path = (String) param.get("path");
        HttpRequest request = HttpUtil.createPost(checkinUrl);
        request.form("photo", FileUtil.file(path), "targetModel", faceModel);
        request.form("code", apiCode);
        HttpResponse response = request.execute();
        if (response.getStatus() != 200) {
            log.error("人脸识别服务异常");
            throw new EmosException("人脸识别服务异常");
        }
        String body = response.body();
        if (Constant.FACE_MODEL_CANNOT_RECOGNIZE.equals(body) ||
                Constant.FACE_MODEL_MULTIPLE_FACES.equals(body)) {
            throw new EmosException(body);
        } else if ("False".equals(body)) {
            throw new EmosException("签到无效, 非本人签到");
        } else if ("True".equals(body)) {
            String address = (String) param.get("address");
            String city = (String) param.get("city");
            String district = (String) param.get("district");

            // 获取签到地区新冠疫情风险等级
            if (checkPandemic) { // 是否需要检查新冠疫情等级
                log.info("查询[{}][{}]的疫情风险等级", city, district);
                if (!StringUtils.isEmpty(city) && !StringUtils.isEmpty(district)) {
                    String code = cityDao.searchCodeByCity(city);
                    String url = String.format(pandemicRiskUrl, code, district);
                    Element doc = jSoupUtil.getDocument(url);
                    Elements elements = doc.getElementsByClass("list-content");
                    if (elements.size() > 0) {
                        Element e = elements.get(0);
                        String risk = e.select("p:last-child").text();
                        if (Constant.PANDEMIC_HIGH_RISK.equals(risk))
                            emailUtil.sendMessage(userId, address);
                    }
                }
            }

            TbCheckin record = new TbCheckin();
            record.setUserId(userId);
            record.setAddress((String) param.get("address"));
            record.setDistrict(district);
            record.setCity(city);
            record.setCountry((String) param.get("country"));
            record.setProvince((String) param.get("province"));
            record.setStatus((byte) status);
            record.setDate(DateUtil.today());
            record.setCreateTime(now);
            checkinDao.insertSelective(record);
        }
    }

    /**
     * 创建人脸识别模型
     */
    @Override
    @Transactional
    public void createFaceModel(Integer userId, String photoPath) {
        log.info("创建用户[{}]人脸识别模型", userId);
        HttpRequest request = HttpUtil.createPost(createFaceModelUrl);
        request.form("photo", FileUtil.file(photoPath));
        request.form("code", apiCode);
        HttpResponse response = request.execute();
        String body = response.body();
        if (Constant.FACE_MODEL_CANNOT_RECOGNIZE.equals(body) ||
                Constant.FACE_MODEL_MULTIPLE_FACES.equals(body)) {
            log.error(body);
            throw new EmosException(body);
        }
        // 创建人脸模型
        TbFaceModel model = new TbFaceModel();
        model.setUserId(userId);
        model.setFaceModel(body);
        faceModelDao.insertSelective(model);
    }

}
