package com.yp.pay.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class WxSingleProfitShareDTO {

    @ApiModelProperty(value = "状态码（通信标志SUCCESS/FAIL）")
    private String resultCode;

    @ApiModelProperty(value = "当resultCode为FAIL时返回错误代码")
    private String errCode;

    @ApiModelProperty(value = "当resultCode为FAIL时返回错误描述")
    private String errCodeMsg;

    @ApiModelProperty(value = "商户支付订单号")
    private String orderNo;

    @ApiModelProperty(value = "平台支付订单号")
    private String platOrderNo;

    @ApiModelProperty(value = "商户分账单号")
    private String profitShareNo;

    @ApiModelProperty(value = "平台分账单号")
    private String platProfitShareNo;

}