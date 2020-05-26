package com.yp.pay.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CloseOrderDTO {

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "业务结果 SUCCESS/FAIL")
    private String resultCode;

    @ApiModelProperty(value = "当resultCode为FAIL时返回错误描述")
    private String resultMsg;
}