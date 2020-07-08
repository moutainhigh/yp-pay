package com.yp.pay.entity.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author: lijiang
 * @date: 2020.02.24 17:50
 * @description: AliWebPayReq
 */
@Data
public class AliWebPayReq {

    @NotBlank(message = "商户号不能为空")
    @ApiModelProperty(value = "商户在平台对应的商户号",required = true)
    private String merchantNo;

    @NotBlank(message = "支付渠道不能为空")
    @ApiModelProperty(value = "支付渠道",required = true)
    private String payWayCode;

    /**
     * 商户订单号,64个字符以内、可包含字母、数字、下划线；需保证在商户端不重复
     */
    @ApiModelProperty(value = "商户订单号(默认为商户号+时间戳)")
    private String outTradeNo;


    @NotBlank(message = "订单标题")
    @ApiModelProperty(value = "订单标题，粗略描述用户的支付目的", required = true)
    private String subject;

    /**
     * 订单总金额，单位为分
     * 如果同时传入
     * 【可打折金额】和【不可打折金额】，该参数可以不用传入； 如果同时传入了【可打折金额】，
     * 【不可打折金额】，【订单总金额】三者，则必须满足如下条件：
     * 【订单总金额】=【可打折金额】+【不可打折金额】
     */
    @NotNull(message = "支付总金额不能为空")
    @ApiModelProperty(value = "订单总金额", required = true)
    private Integer totalAmount;

    /**
     * 收款支付宝用户ID。 如果该值为空，则默认为商户签约账号对应的支付宝用户ID
     */
    @ApiModelProperty(value = "sellerId", hidden = true)
    private String  sellerId;

    @ApiModelProperty(value = "quitUrl", hidden = true)
    private String quitUrl;

    @ApiModelProperty(value = "productCode", hidden = true)
    private String productCode;


    //************************************可选项****************

    /**
     * 说明：该笔订单允许的最晚付款时间，逾期将关闭交易。
     * 取值范围：1m～15d。m-分钟，h-小时，d-天，
     * 1c-当天（1c-当天的情况下，无论交易何时创建，都在0点关闭）。
     * 该参数数值不接受小数点， 如 1.5h，可转换为 90m
     */
    @ApiModelProperty(value = "该笔订单允许的最晚付款时间，默认5m", hidden = true)
    private String timeoutExpress;

    @ApiModelProperty(value = "绝对超时时间", hidden = true)
    private String timeExpire;

    @ApiModelProperty(value = "针对用户授权接口，获取用户相关数据时，用于标识用户授权关系", hidden = true)
    private String authToken;

    /**
     * 公用回传参数，如果请求时传递了该参数，则返回给商户时会回传该参数。支付宝只会在同步返回
     * （包括跳转回商户网站）和异步通知时将该参数原样返回。本参数必须进行UrlEncode之后才可以发送给支付宝。
     */
    @ApiModelProperty(value = "回传参数")
    private String attach;

    /**
     * 订单描述
     */
    @ApiModelProperty(value = "订单描述", example = "购买商品3件共20.00元")
    private String body;


    //************************************隐藏项****************

    @ApiModelProperty(value = "创建人", hidden = true)
    private String createUser;

    @ApiModelProperty(value = "支付方式",hidden = true)
    private String payTypeCode;

}
