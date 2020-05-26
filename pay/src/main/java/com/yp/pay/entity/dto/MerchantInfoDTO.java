package com.yp.pay.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MerchantInfoDTO {

    @ApiModelProperty(value = "记录号")
    protected Long sysno;

    @ApiModelProperty(value = "商户编号")
    private String merchantNo;

    @ApiModelProperty(value = "商户名称")
    private String merchantName;

    @ApiModelProperty(value = "客户编号")
    private Long customerSysno;

    @ApiModelProperty(value = "支付宝微信appId")
    private String appId;

    @ApiModelProperty(value = "支付宝partnerId即pid")
    private String partnerId;

    @ApiModelProperty(value = "秘钥(API秘钥，支付宝key)")
    private String partnerKey;

    @ApiModelProperty(value = "支付渠道代码 微信：WX_PAY 支付宝：ALI_PAY")
    private String payWayCode;

    @ApiModelProperty(value = "微信APP秘钥，支付宝用户私钥")
    private String rsaPrivateKey;

    @ApiModelProperty(value = "支付宝用户公钥")
    private String rsaPublicKey;

    @ApiModelProperty(value = "支付宝公钥")
    private String aliPublicKey;

    @ApiModelProperty(value = "渠道商户号（微信商户号）")
    private String subMerchantId;

    @ApiModelProperty(value = "费率")
    private String payRate;

    @ApiModelProperty(value = "商户证书路径")
    private String certPath;

    @ApiModelProperty(value = "商户状态 1正常，0冻结")
    private Integer status;

    @ApiModelProperty(value = "限额")
    private BigDecimal maxOrderAmount;

    @ApiModelProperty(value = "支付成功中台页面返回地址")
    private String returnUrl;

    @ApiModelProperty(value = "退出支付中台页面返回地址")
    private String quitUrl;

    @ApiModelProperty(value = "中台接收异步通知地址")
    private String notifyUrl;

    @ApiModelProperty(value = "商户支付接收异步通知地址")
    private String merNotifyUrl;

    @ApiModelProperty(value = "商户退款接收异步通知地址")
    private String merRefundNotifyUrl;
}