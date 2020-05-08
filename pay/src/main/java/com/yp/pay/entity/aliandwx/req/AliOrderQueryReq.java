package com.yp.pay.entity.aliandwx.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AliOrderQueryReq {

    @NotBlank(message = "商户编号不能为空")
    @ApiModelProperty(value = "商户编号（统一分配）",required = true)
    private String merchantNo;

    @ApiModelProperty(value = "商户订单号(和微信订单号选填一项)",required = true)
    private String orderNo;

    @ApiModelProperty(value = "微信订单号(和商户订单号选填一项)",hidden = true)
    private String channelOrderNo;

}
