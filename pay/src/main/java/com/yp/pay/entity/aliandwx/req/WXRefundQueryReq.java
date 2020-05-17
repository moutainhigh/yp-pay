package com.yp.pay.entity.aliandwx.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class WXRefundQueryReq {

    @NotBlank(message = "商户编号不能为空")
    @ApiModelProperty("商户编号（统一分配）")
    private String merchantNo;

    @ApiModelProperty(value = "商户退款单号(和[微信退款单号]选填一项)")
    private String refundOrderNo;

    @ApiModelProperty(value = "微信退款单号(和[商户退款单号]选填一项)")
    private String channelRefundOrderNo;

}
