package com.cunjun.personal.emos.wx.common;

/**
 * Created by CunjunWang on 2021/1/28.
 */
public class Constant {

    /* 返回字段 token */
    public static final String RETURN_VALUE_TOKEN = "token";

    /* 返回字段 permissions */
    public static final String RETURN_VALUE_PERMISSIONS = "permissions";

    /* 超级管理员的激活码 */
    public static final String ROOT_REGISTRATION_CODE = "000000";

    /* 日期名称相关常量 */
    public static final String DATETIME_NOW = "now";
    public static final String DATETIME_ATTENDANCE_START = "attendanceStart"; // 上班考勤开始时间
    public static final String DATETIME_ATTENDANCE_END = "attendanceEnd"; // 上班考勤结束时间
    public static final String DATETIME_CLOSING_START = "closingStart"; // 下班考勤开始时间
    public static final String DATETIME_CLOSING_END = "closingEnd"; // 下班考勤结束时间


    /* 人脸识别服务相关 */
    public static final String FACE_MODEL_CANNOT_RECOGNIZE = "无法识别出人脸";
    public static final String FACE_MODEL_MULTIPLE_FACES = "照片中存在多张人脸";
    public static final String FACE_MODEL_SERVICE_ERROR = "人脸识别服务异常";
    public static final String FACE_MODEL_NO_CORRESPONDING_MODEL = "不存在人脸模型";
    public static final String FACE_MODEL_RESULT_FALSE = "False";
    public static final String FACE_MODEL_RESULT_TRUE = "True";

    /* 考勤相关常量 */
    public static final String CHECKIN_DAY_WORKDAY = "工作日";
    public static final String CHECKIN_DAY_HOLIDAY = "节假日";
    public static final String CHECKIN_ERROR_NOT_SELF_OPERATION = "签到无效, 非本人签到";
    public static final String CHECKIN_LOCATION_PANDEMIC_HIGH_RISK = "高风险";
    public static final String CHECKIN_DUPLICATE_OPERATION = "您今天已经签到过了";
    public static final String CHECKIN_NO_NEED_FOR_HOLIDAY = "节假日不需要考勤";
    public static final String CHECKIN_TOO_EARLY_FOR_ATTENDANCE = "未到上班考勤开始时间";
    public static final String CHECKIN_TOO_LATE_FOR_ATTENDANCE = "超出了上班考勤结束时间";
    public static final String CHECKIN_OK_TO_CHECKIN = "可以考勤";
}
