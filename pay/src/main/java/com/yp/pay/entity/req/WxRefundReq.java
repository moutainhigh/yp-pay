package com.yp.pay.entity.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @description: 订单退款实体类
 *
 * @author: liuX
 * @time: 2020/5/31 9:31
 */
@Data
public class WxRefundReq {

    @NotBlank(message = "商户号不能为空")
    @ApiModelProperty("商户编号（统一分配）")
    private String merchantNo;

    @ApiModelProperty("商户订单号（原商户支付订单号，与平台订单号选填一项)")
    private String originalOrderNo;

    @ApiModelProperty("平台订单号（原平台支付订单号，与商户订单号选填一项)")
    private String originalPlatOrderNo;

    @ApiModelProperty(value = "微信订单号（原微信支付订单号)",hidden = true)
    private String originalChannelOrderNo;

    @NotBlank(message = "商户退款单号不能为空（不能重复）")
    @ApiModelProperty(value = "商户退款单号")
    private String refundNo;

    @ApiModelProperty(value = "订单金额(注：单位为分)",hidden = true)
    private Integer amount;

    @NotNull(message = "退款金额不能为空")
    @ApiModelProperty(value = "退款金额(注：单位为分)")
    private Integer refundAmount;

}
