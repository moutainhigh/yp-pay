package com.yp.pay.entity.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class WxUnifiedPayReq {

    @NotBlank(message = "商户编号不能为空")
    @ApiModelProperty(value = "商户编号（统一分配）")
    private String merchantNo;

    @ApiModelProperty("设备信息(商户自定义，如门店编号)")
    private String deviceInfo;

    @NotBlank(message = "商品描述不能为空")
    @ApiModelProperty("商品描述(需要按照微信格式：image形象店-深圳腾大- QQ公仔)")
    private String subject;

    @ApiModelProperty("商品详情")
    private String detail;

    @ApiModelProperty("附加数据(在查询API和支付通知中原样返回，该字段主要用于商户携带订单的自定义数据)")
    private String attach;

    @NotBlank(message = "订单号不能为空")
    @ApiModelProperty("订单号(商户系统内部订单号，要求32个字符内，只能是数字、大小写字母且在同一个商户号下唯一)")
    private String orderNo;

    @NotBlank(message = "终端IP不能为空")
    @ApiModelProperty("终端IP")
    private String clientIp;

    @NotNull(message = "金额不能为空")
    @ApiModelProperty(value = "金额(注：单位为分)")
    private Integer amount;

    @NotBlank(message = "交易类型不能为空")
    @ApiModelProperty(value = "交易类型 注：JSAPI:JSAPI支付 NATIVE:Native支付 APP:APP支付")
    private String tradeType;

    @ApiModelProperty(value = "是否是聚合支付 注：交易类型为JSAPI的时候该值才存在 true:聚合支付 false:非聚合支付")
    private Boolean aggregationPay;

    @ApiModelProperty(value = "tradeType=JSAPI时（即JSAPI支付），此参数必传，此参数为微信用户在商户对应appid下的唯一标识。")
    private String openId;

    /**
     *  调用方不需要传，通过接口区分
     */
    @ApiModelProperty(value = "是否分账",hidden = true)
    private String profitShare;
}
