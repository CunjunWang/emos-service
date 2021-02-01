package com.cunjun.personal.emos.wx.db.pojo;

import java.io.Serializable;
import lombok.Data;
import lombok.ToString;

/**
 * tb_city
 * @author 
 */
@Data
@ToString
public class TbCity implements Serializable {
    /**
     * 主键
     */
    private Integer id;

    /**
     * 城市名称
     */
    private String city;

    /**
     * 拼音简称
     */
    private String code;

    private static final long serialVersionUID = 1L;
}