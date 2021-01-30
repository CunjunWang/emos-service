package com.cunjun.personal.emos.wx.common;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * Created by CunjunWang on 2021/1/30.
 */
@Data
@Component
public class SystemConstants {

    /* 上班打卡开始时间 */
    public String attendanceStartTime;

    /* 上班打卡时间 */
    public String attendanceTime;

    /* 上班打卡结束时间 */
    public String attendanceEndTime;

    /* 下班打卡开始时间 */
    public String closingStartTime;

    /* 下班打卡时间 */
    public String closingTime;

    /* 下班打卡结束时间 */
    public String closingEndTime;

}
