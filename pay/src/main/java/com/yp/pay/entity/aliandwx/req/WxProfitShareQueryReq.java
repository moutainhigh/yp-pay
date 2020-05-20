package com.yp.pay.entity.aliandwx.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class WxProfitShareQueryReq {

    @NotBlank(message = "商户编号不能为空")
    @ApiModelProperty(value = "商户编号（统一分配）")
    private String merchantNo;

    @NotBlank(message = "渠道支付订单号不能为空")
    @ApiModelProperty("渠道支付订单号")
    private String channelOrderNo;

    @ApiModelProperty("渠道分账单号")
    private String channelProfitShareOrderNo;

}
