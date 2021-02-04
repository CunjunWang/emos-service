package com.cunjun.personal.emos.wx.service.inf;

/**
 * Created by CunjunWang on 2021/2/4.
 */
public interface IFaceModelService {

    /**
     * 创建人脸模型
     */
    String createFaceModel(String photoPath);

    /**
     * 使用模型进行识别签到
     */
    String checkin(String path, String model);

}
