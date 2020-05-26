package com.yp.pay.entity.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: lijiang
 * @date: 2020.02.25 10:02
 * @description: AliWebRefundReq
 */
@Data
public class AliWebRefundReq {
    @ApiModelProperty(value = "原交易订单号")
    private String outTradeNo;

    private BigDecimal refundAmount;

    private String refundReason;

    private String outRequestNo;
}
