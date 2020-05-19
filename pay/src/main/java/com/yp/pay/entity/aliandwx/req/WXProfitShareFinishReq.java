package com.yp.pay.entity.aliandwx.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class WXProfitShareFinishReq {

    @NotBlank(message = "渠道订单号不能为空")
    @ApiModelProperty(value = "渠道订单号")
    private String channelOrderNo;

    @NotBlank(message = "商户分账订单号不能为空")
    @ApiModelProperty(value = "商户分账订单号")
    private String 	profitShareOrderNo;

    @NotBlank(message = "分账完结描述不能为空")
    @ApiModelProperty(value = "分账完结描述")
    private String description;
}
