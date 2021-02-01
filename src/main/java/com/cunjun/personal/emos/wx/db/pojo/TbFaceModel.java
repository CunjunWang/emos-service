package com.cunjun.personal.emos.wx.db.pojo;

import java.io.Serializable;

import lombok.Data;
import lombok.ToString;

/**
 * tb_face_model
 *
 * @author
 */
@Data
@ToString
public class TbFaceModel implements Serializable {
    /**
     * 主键值
     */
    private Integer id;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 用户人脸模型
     */
    private String faceModel;

    private static final long serialVersionUID = 1L;
}