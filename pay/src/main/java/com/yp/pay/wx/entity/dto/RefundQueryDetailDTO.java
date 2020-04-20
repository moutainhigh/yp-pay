package com.yp.pay.wx.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class RefundQueryDetailDTO {

    @ApiModelProperty(value = "商户退款单号")
    private String refundOrderNo;

    @ApiModelProperty(value = "微信退款单号")
    private String 	channelRefundOrderNo;

    @ApiModelProperty(value = "申请退款金额")
    private BigDecimal refundFee;

    @ApiModelProperty(value = "退款金额",notes = "当退款状态为退款成功时有返回。")
    private BigDecimal settlementRefundFee;

    @ApiModelProperty(value = "退款状态",notes = "SUCCESS—退款成功 REFUNDCLOSE—退款关闭。 PROCESSING—退款处理中 CHANGE—退款异常，退款到银行发现用户的卡作废或者冻结了，导致原路退款银行卡失败，可前往商户平台（pay.weixin.qq.com）-交易中心，手动处理此笔退款。")
    private String refundStatus;

    @ApiModelProperty(value = "退款入账账户",notes = "1）退回银行卡：{银行名称}{卡类型}{卡尾号} 2）退回支付用户零钱: 支付用户零钱 3）退还商户: 商户基本账户 商户结算银行账户 4）退回支付用户零钱通: 支付用户零钱通")
    private String refundRecvAccount;

    @ApiModelProperty(value = "退款成功时间",notes = "当退款状态为退款成功时有返回。")
    private Date refundSuccessTime;
}
