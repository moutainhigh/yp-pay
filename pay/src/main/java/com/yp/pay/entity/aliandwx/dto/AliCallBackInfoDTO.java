package com.yp.pay.entity.aliandwx.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AliCallBackInfoDTO {

    @ApiModelProperty(value = "是否处理完成")
    private Boolean dealSuccess;

    @ApiModelProperty(value = "处理描述")
    private String message;

    @ApiModelProperty(value = "异步通知地址")
    private String url;

    @ApiModelProperty(value = "异步通知数据详细信息,只有当dealSuccess为true的时候才有详细信息")
    private AliCallBackInfoDetailDTO aliCallBackInfoDetailDTO;

}