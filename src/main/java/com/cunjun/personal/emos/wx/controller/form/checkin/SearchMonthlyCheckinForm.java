package com.cunjun.personal.emos.wx.controller.form.checkin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by CunjunWang on 2021/2/5.
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "查询用户月签到数据表单", description = "查询用户月签到数据表单")
public class SearchMonthlyCheckinForm implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "年不能为空")
    @Range(min = 2000, max = 3000, message = "年份超出合法查询范围")
    @ApiModelProperty(value = "年", name = "year")
    private Integer year;

    @NotNull(message = "月不能为空")
    @Range(min = 1, max = 12, message = "月份超出合法查询范围")
    @ApiModelProperty(value = "月", name = "month")
    private Integer month;

}
