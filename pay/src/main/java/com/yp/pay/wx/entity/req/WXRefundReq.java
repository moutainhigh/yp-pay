package com.yp.pay.wx.entity.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class WXRefundReq{

    @NotBlank(message = "商户号不能为空")
    @ApiModelProperty("商户编号（统一分配）")
    private String merchantNo;

    @ApiModelProperty("微信订单号（原微信支付订单号，与下一项商户订单号选填一项)")
    private String originalChannelOrderNo;

    @ApiModelProperty("商户订单号（原商户支付订单号，与上一项微信订单号选填一项)")
    private String originalOrderNo;

    @NotBlank(message = "商户退款单号不能为空（不能重复）")
    @ApiModelProperty(value = "商户退款单号")
    private String refundOrderNo;

    @NotNull(message = "订单金额不能为空")
    @ApiModelProperty(value = "订单金额(注：单位为元，最多两位小数)")
    private BigDecimal amount;

    @NotNull(message = "退款金额不能为空")
    @ApiModelProperty(value = "退款金额(注：单位为元，最多两位小数)")
    private BigDecimal refundAmount;

}
