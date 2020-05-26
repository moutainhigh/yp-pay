package com.yp.pay.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AliCallBackInfoDetailDTO {

    @ApiModelProperty(value = "商户编号")
    private String merchantNo;

    @ApiModelProperty(value = "订单总金额 单位：元")
    private String totalAmount;

    @ApiModelProperty(value = "卖家实收金额 单位：元")
    private String receiptAmount;

    @ApiModelProperty(value = "货币种类，默认人民币：CNY")
    private String feeType;

    @ApiModelProperty(value = "支付宝支付订单号")
    private String transactionId;

    @ApiModelProperty(value = "商户订单号")
    private String orderNo;

    @ApiModelProperty(value = "商家数据包")
    private String attach;

    @ApiModelProperty(value = "买家支付宝用户号")
    private String buyerId;

    @ApiModelProperty(value = "买家支付宝账户号")
    private String buyerLoginId;

    @ApiModelProperty(value = "该笔交易创建的时间。格式为yyyy-MM-dd HH:mm:ss")
    private String gmtCreate;

    @ApiModelProperty(value = "该笔交易创建的时间。格式为yyyy-MM-dd HH:mm:ss")
    private String gmtPayment;

    @ApiModelProperty(value = "该笔交易的退款时间。格式为yyyy-MM-dd HH:mm:ss")
    private String gmtRefund;

    @ApiModelProperty(value = "该笔交易结束时间。格式为yyyy-MM-dd HH:mm:ss")
    private String gmtClose;

    @ApiModelProperty(value = "微信:WX_PAY,支付宝:ALI_PAY")
    private String payType;

    @ApiModelProperty(value = "业务结果 SUCCESS/FAIL")
    private String resultCode;

    @ApiModelProperty(value = "错误代码")
    private String errCode;

    @ApiModelProperty(value = "错误代码描述")
    private String errCodeDes;

}