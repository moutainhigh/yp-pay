package com.yp.pay.entity.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author: lijiang
 * @date: 2020.02.26 11:06
 * @description: MerchantQueryReq
 */
@Data
public class MerchantQueryReq {

    @NotBlank(message = "商户号不能为空")
    @ApiModelProperty(value = "商户号")
    private String merchantNo;

    @NotBlank(message = "支付方式不能为空")
    @ApiModelProperty(value = "支付方式")
    private String payWayCode;
}
