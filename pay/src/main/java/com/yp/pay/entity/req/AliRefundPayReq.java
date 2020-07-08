package com.yp.pay.entity.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class AliRefundPayReq {

    @NotBlank(message = "商户编号不能为空")
    @ApiModelProperty(value = "商户编号（统一分配）",required = true)
    private String merchantNo;

    @ApiModelProperty(value = "商户订单号(和微信订单号选填一项)",required = true)
    private String orderNo;

    @NotNull(message = "退款金额不能为空")
    @ApiModelProperty(value = "退款金额", required = true)
    private Integer refundAmount;

    @ApiModelProperty(value = "退款订单号", required = true)
    private String refundOrderNo;

    @ApiModelProperty(value = "退款原因")
    private String refundReason;

}
