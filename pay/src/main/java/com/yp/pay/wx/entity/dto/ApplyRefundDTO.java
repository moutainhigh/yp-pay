package com.yp.pay.wx.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ApplyRefundDTO {

    @ApiModelProperty(value = "业务结果 SUCCESS/FAIL")
    private String resultCode;

    @ApiModelProperty(value = "当resultCode为FAIL时返回错误代码")
    private String errCode;

    @ApiModelProperty(value = "当resultCode为FAIL时返回错误描述")
    private String errCodeMsg;

    @ApiModelProperty(value = "微信订单号")
    private String transactionId;

    @ApiModelProperty(value = "商户订单号")
    private String orderNo;

    @ApiModelProperty(value = "商户退款单号")
    private String refundOrderNo;

    @ApiModelProperty(value = "微信退款单号")
    private String refundId;

    @ApiModelProperty(value = "退款金额")
    private String refundFee;

    @ApiModelProperty(value = "订单金额")
    private String totalFee;

    @ApiModelProperty(value = "货币种类 默认人民币：CNY")
    private String feeType;

    @ApiModelProperty(value = "现金支付金额")
    private String cashFee;

}