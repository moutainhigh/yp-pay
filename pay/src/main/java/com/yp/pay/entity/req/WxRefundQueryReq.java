package com.yp.pay.entity.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @description: 商户退款查询请求实体类
 *
 * @author: liuX
 * @time: 2020/5/31 10:29
 */
@Data
public class WxRefundQueryReq extends CommonReq {

    @ApiModelProperty(value = "商户支付单号",hidden = true)
    private String platOrderNo;

    @ApiModelProperty(value = "微信支付订单号",hidden = true)
    private String channelOrderNo;

    @ApiModelProperty(value = "商户退款单号(和[平台退款单号]选填一项)")
    private String refundNo;

    @ApiModelProperty(value = "平台退款单号(和[商户退款单号]选填一项)")
    private String platRefundNo;

    @ApiModelProperty(value = "微信退款单号",hidden = true)
    private String channelRefundNo;

}
