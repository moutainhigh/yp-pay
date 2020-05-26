package com.yp.pay.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RefundCallBackInfoDetailDTO {

    @ApiModelProperty(value = "商户编号")
    private String merchantNo;

    @ApiModelProperty(value = "商户订单号（原商户支付订单号）")
    private String originalOrderNo;

    @ApiModelProperty(value = "渠道订单号（原渠道支付订单号）")
    private String originalTransactionId;

    @ApiModelProperty(value = "商户退款单号")
    private String refundOrderNo;

    @ApiModelProperty(value = "渠道退款单号")
    private String channelRefundOrderNo;

    @ApiModelProperty(value = "订单金额")
    private String totalFee;

    @ApiModelProperty(value = "申请退款金额")
    private String refundFee;

    @ApiModelProperty(value = "退款金额")
    private String settlementRefundFee;

    @ApiModelProperty(value = "退款状态 SUCCESS-退款成功 CHANGE-退款异常 REFUNDCLOSE—退款关闭")
    private String refundStatus;

    @ApiModelProperty(value = "退款成功时间")
    private String successTime;

    @ApiModelProperty(value = "退款入账账户 1）退回银行卡： {银行名称}{卡类型}{卡尾号} 2）退回支付用户零钱: 支付用户零钱 " +
            "3）退还商户:a商户基本账户 b商户结算银行账户 4）退回支付用户零钱通: 支付用户零钱通")
    private String refundRecvAccount;

    @ApiModelProperty(value = "退款资金来源 REFUND_SOURCE_RECHARGE_FUNDS 可用余额退款/基本账户 REFUND_SOURCE_UNSETTLED_FUNDS 未结算资金退款")
    private String refundAccount;

    @ApiModelProperty(value = "退款发起来源 API接口 VENDOR_PLATFORM商户平台")
    private String refundRequestSource;

    @ApiModelProperty(value = "微信:WX_PAY,支付宝:ALI_PAY")
    private String payType;
}