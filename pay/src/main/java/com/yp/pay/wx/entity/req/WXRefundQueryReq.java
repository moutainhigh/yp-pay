package com.yp.pay.wx.entity.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class WXRefundQueryReq{

    @NotBlank(message = "商户编号不能为空")
    @ApiModelProperty("商户编号（统一分配）")
    private String merchantNo;

    @ApiModelProperty("微信订单号(和[商户订单号/商户退款单号/微信退款单号]选填一项)")
    private String originalChannelOrderNo;

    @ApiModelProperty("商户订单号(和[微信订单号/商户退款单号/微信退款单号]选填一项)")
    private String originalOrderNo;

    @ApiModelProperty(value = "商户退款单号(和[微信订单号/商户订单号/微信退款单号]选填一项)")
    private String refundOrderNo;

    @ApiModelProperty(value = "微信退款单号(和[微信订单号/商户订单号/商户退款单号]选填一项)")
    private String channelRefundOrderNo;

    @ApiModelProperty(value = "偏移量",notes = "若不传，默认为0 当部分退款次数超过10次时可使用，表示返回的查询结果从这个偏移量开始取记录")
    private int offset;
}
