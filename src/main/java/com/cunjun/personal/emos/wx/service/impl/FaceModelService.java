package com.cunjun.personal.emos.wx.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.cunjun.personal.emos.wx.common.Constant;
import com.cunjun.personal.emos.wx.exception.EmosException;
import com.cunjun.personal.emos.wx.service.inf.IFaceModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

/**
 * Created by CunjunWang on 2021/2/4.
 */
@Slf4j
@Service
@PropertySource(value = {"classpath:application.yml", "classpath:secret.properties"})
public class FaceModelService implements IFaceModelService {

    @Value("${emos.face.create-face-model-url}")
    private String createFaceModelUrl;

    @Value("${emos.face.checkin-url}")
    private String checkinUrl;

    @Value("${course.api-code}")
    private String apiCode;

    /**
     * 创建人脸模型
     */
    @Override
    public String createFaceModel(String photoPath) {
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
        return body;
    }

    /**
     * 使用模型进行识别签到
     */
    @Override
    public String checkin(String path, String model) {
        HttpRequest request = HttpUtil.createPost(checkinUrl);
        request.form("photo", FileUtil.file(path), "targetModel", model);
        request.form("code", apiCode);
        HttpResponse response = request.execute();
        if (response.getStatus() != 200) {
            log.error(Constant.FACE_MODEL_SERVICE_ERROR);
            throw new EmosException(Constant.FACE_MODEL_SERVICE_ERROR);
        }

        String body = response.body();
        if (Constant.FACE_MODEL_CANNOT_RECOGNIZE.equals(body) ||
                Constant.FACE_MODEL_MULTIPLE_FACES.equals(body))
            throw new EmosException(body);
        if (Constant.FACE_MODEL_RESULT_FALSE.equals(body))
            throw new EmosException(Constant.CHECKIN_ERROR_NOT_SELF_OPERATION);

        return body;
    }

}
