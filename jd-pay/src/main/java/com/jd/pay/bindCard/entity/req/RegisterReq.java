package com.jd.pay.bindCard.entity.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class RegisterReq {

    @NotBlank(message = "用户编号不能为空")
    @ApiModelProperty(value = "用户编号（唯一）")
    public String userCode;

    @NotBlank(message = "手机号不能为空")
    @ApiModelProperty(value = "手机号")
    public String cellPhone;

}
