package com.yp.pay.entity.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class WxCloseOrderReq {

    @NotBlank(message = "商户编号不能为空")
    @ApiModelProperty(value = "商户编号（统一分配）",required = true)
    private String merchantNo;

    @NotBlank(message = "商户订单号不能为空")
    @ApiModelProperty(value = "商户订单号",required = true)
    private String orderNo;

}
