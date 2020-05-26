package com.yp.pay.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * App支付返回调用方信息
 */
@Data
public class WxAppPayDTO {

    @ApiModelProperty(value = "微信AppId")
    private String appId;

    @ApiModelProperty(value = "微信商户号")
    private String merId;

    @ApiModelProperty(value = "预支付交易会话标识(微信生成的预支付会话标识，用于后续接口调用中使用，该值有效期为2小时)")
    private String prepayId;

    @ApiModelProperty(value = "随机字符串")
    private String nonceStr;

    @ApiModelProperty(value = "时间戳（到秒的时间戳，长度为10位）")
    private String timeStamp;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "签名")
    private String sign;
}