package com.yp.pay.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RefundCallBackInfoDTO {

    @ApiModelProperty(value = "是否处理完成")
    private boolean dealSuccess;

    @ApiModelProperty(value = "处理描述")
    private String message;

    @ApiModelProperty(value = "异步通知地址")
    private String url;

    @ApiModelProperty(value = "异步通知数据详细信息,只有当dealSuccess为true的时候才有详细信息")
    private RefundCallBackInfoDetailDTO refundCallBackInfoDetailDTO;

}