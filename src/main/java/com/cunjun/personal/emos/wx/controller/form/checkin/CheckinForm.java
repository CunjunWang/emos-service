package com.cunjun.personal.emos.wx.controller.form.checkin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by CunjunWang on 2021/2/1.
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "用户签到表单", description = "用户签到表单")
public class CheckinForm implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "具体地址", name = "address")
    private String address;

    @ApiModelProperty(value = "国家", name = "country")
    private String country;

    @ApiModelProperty(value = "省份", name = "province")
    private String province;

    @ApiModelProperty(value = "城市", name = "city")
    private String city;

    @ApiModelProperty(value = "区", name = "district")
    private String district;

}
