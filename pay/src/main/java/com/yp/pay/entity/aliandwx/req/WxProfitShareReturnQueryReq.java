package com.yp.pay.entity.aliandwx.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class WxProfitShareReturnQueryReq {

    @NotBlank(message = "商户编号不能为空")
    @ApiModelProperty(value = "商户编号（统一分配）")
    private String merchantNo;

    @ApiModelProperty("商户分账单号（和渠道分账单号选填一项）")
    private String profitShareOrderNo;

    @ApiModelProperty("渠道分账单号")
    private String channelProfitShareOrderNo;

    @NotBlank(message = "商户回退单号不能为空")
    @ApiModelProperty(value = "商户回退单号）")
    private String profitShareReturnOrderNo;

}
