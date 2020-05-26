package com.yp.pay.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CallBackInfoDetailDTO {

    @ApiModelProperty(value = "商户编号")
    private String merchantNo;

    @ApiModelProperty(value = "订单金额 单位：元")
    private String totalFee;

    @ApiModelProperty(value = "现金支付金额 单位：元")
    private String cashFee;

    @ApiModelProperty(value = "货币种类，默认人民币：CNY")
    private String feeType;

    @ApiModelProperty(value = "微信支付订单号")
    private String transactionId;

    @ApiModelProperty(value = "商户订单号")
    private String orderNo;

    @ApiModelProperty(value = "商家数据包")
    private String attach;

    @ApiModelProperty(value = "支付完成时间")
    private String timeEnd;

    @ApiModelProperty(value = "微信:WX_PAY,支付宝:ALI_PAY")
    private String payType;

    @ApiModelProperty(value = "用户标识")
    private String openId;

    @ApiModelProperty(value = "业务结果 SUCCESS/FAIL")
    private String resultCode;

    @ApiModelProperty(value = "错误代码")
    private String errCode;

    @ApiModelProperty(value = "错误代码描述")
    private String errCodeDes;

}