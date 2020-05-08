package com.yp.pay.entity.aliandwx.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author: lijiang
 * @date: 2019.12.10 17:50
 * @description: AliF2FPayReq
 */
@Data
public class AliF2FPayReq {

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

    /**
     * 支付授权码，25~30开头的长度为16~24位的数字，实际字符串长度以开发者获取的付款码长度为准
     */
    @NotBlank(message = "支付授权码不能为空")
    @ApiModelProperty(value = "支付授权码", required = true)
    private String authCode;

    @NotBlank(message = "订单标题")
    @ApiModelProperty(value = "订单标题，粗略描述用户的支付目的", required = true, example = "xxx品牌xxx门店当面付消费")
    private String subject;

    /**
     * 订单总金额，单位为元，精确到小数点后两位，取值范围[0.01,100000000] 如果同时传入
     * 【可打折金额】和【不可打折金额】，该参数可以不用传入； 如果同时传入了【可打折金额】，
     * 【不可打折金额】，【订单总金额】三者，则必须满足如下条件：
     * 【订单总金额】=【可打折金额】+【不可打折金额】
     */
    @NotNull(message = "支付总金额不能为空")
    @ApiModelProperty(value = "订单总金额", required = true)
    private BigDecimal totalAmount;


    @ApiModelProperty(value = "门店id", required = true)
    private String  storeId;


    //************************************可选项****************
    /**
     * 说明：该笔订单允许的最晚付款时间，逾期将关闭交易。
     * 取值范围：1m～15d。m-分钟，h-小时，d-天，
     * 1c-当天（1c-当天的情况下，无论交易何时创建，都在0点关闭）。
     * 该参数数值不接受小数点， 如 1.5h，可转换为 90m
     */
    @ApiModelProperty(value = "该笔订单允许的最晚付款时间，默认5m", hidden = true)
    private String timeoutExpress;

    /**
     * 订单描述
     */
    @ApiModelProperty(value = "订单描述", example = "购买商品3件共20.00元")
    private String body;

    @ApiModelProperty(value = "商品明细")
    private List<PlatGoodsDetail> platGoods;


    //************************************隐藏项****************

    @ApiModelProperty(value = "创建人", hidden = true)
    private String createUser;
    /**
     * 支付场景 条码支付，取值：bar_code 声波支付，取值：wave_code
     */
    @ApiModelProperty(value = "支付场景", hidden = true)
    private String scene;

    @ApiModelProperty(value = "支付方式",hidden = true)
    private String payTypeCode;

    /**
     * 参与优惠计算的金额，单位为元，精确到小数点后两位，取值范围[0.01,100000000]。 如果该值未传入，
     * 但传入了【订单总金额】和【不可打折金额】，则该值默认为【订单总金额】-【不可打折金额】
     */
    @ApiModelProperty(value = "参与优惠计算的金额", hidden = true)
    private BigDecimal discountableAmount;

    /**
     * 不参与优惠计算的金额，单位为元，精确到小数点后两位，取值范围[0.01,100000000]。如果该值未传入，
     * 但传入了【订单总金额】和【可打折金额】，则该值默认为【订单总金额】-【可打折金额】
     */
    @ApiModelProperty(value = "不参与优惠计算的金额", hidden = true)
    private BigDecimal undiscountableAmount;

}
