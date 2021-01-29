package com.cunjun.personal.emos.wx.controller.form.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * Created by CunjunWang on 2021/1/29.
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "用户登录表单", description = "用户登录表单")
public class LoginForm implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "微信临时授权码不能为空")
    @ApiModelProperty(value = "微信临时授权码", name = "wxCode")
    private String wxCode;

}
