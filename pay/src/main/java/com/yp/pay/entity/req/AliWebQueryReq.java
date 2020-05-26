package com.yp.pay.entity.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author: lijiang
 * @date: 2020.02.25 10:12
 * @description: AliWebQueryReq
 */
@Data
public class AliWebQueryReq {

    @NotBlank(message = "商户交易订单不能为空")
    @ApiModelProperty(value = "商户交易订单号")
    private String outTradeNo;

    @ApiModelProperty(value = "渠道交易号", hidden = true)
    private String tradeNo;

    @NotBlank(message = "商户号不能为空")
    @ApiModelProperty(value = "商户号")
    private String merchantNo;
}
