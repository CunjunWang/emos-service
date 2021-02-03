package com.cunjun.personal.emos.wx.service.inf;

import java.util.HashMap;
import java.util.List;

/**
 * Created by CunjunWang on 2021/1/30.
 */
public interface ICheckinService {

    /**
     * 判定是否是合适的签到时间
     */
    String validTimeToCheckin(Integer userId, String date);

    /**
     * 用户签到
     */
    void checkin(HashMap<String, Object> param);

    /**
     * 创建人脸识别模型
     */
    void createFaceModel(Integer userId, String photoPath);

    /**
     * 查询用户当天签到状态
     */
    HashMap<String, Object> searchUserTodayCheckin(Integer userId);

    /**
     * 查询用户总签到天数
     */
    Long searchUserTotalCheckinDays(Integer userId);

    /**
     * 查询用户一周内的签到记录
     */
    List<HashMap<String, String>> searchUserWeeklyCheckinResult(HashMap<String, Object> param);
}
