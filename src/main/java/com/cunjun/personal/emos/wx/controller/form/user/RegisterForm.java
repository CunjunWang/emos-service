package com.cunjun.personal.emos.wx.controller.form.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * Created by CunjunWang on 2021/1/29.
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "用户注册表单", description = "用户注册表单")
public class RegisterForm implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "注册邀请码不能为空")
    @Pattern(regexp = "^[0-9]{6}$", message = "注册邀请码必须是6位数字")
    @ApiModelProperty(value = "注册邀请码", name = "registrationCode")
    private String registrationCode;

    @NotBlank(message = "微信临时授权码不能为空")
    @ApiModelProperty(value = "微信临时授权码", name = "wxCode")
    private String wxCode;

    @NotBlank(message = "昵称不能为空")
    @ApiModelProperty(value = "昵称", name = "nickname")
    private String nickname;

    @NotBlank(message = "头像链接不能为空")
    @ApiModelProperty(value = "头像链接", name = "avatarUrl")
    private String avatarUrl;

}
