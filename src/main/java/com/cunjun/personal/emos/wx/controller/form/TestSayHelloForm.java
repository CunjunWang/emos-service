package com.cunjun.personal.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * Created by CunjunWang on 2021/1/26.
 */
@Data
@ApiModel
public class TestSayHelloForm {

    @NotBlank
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{2,15}$")
    @ApiModelProperty("姓名")
    private String name;

}
