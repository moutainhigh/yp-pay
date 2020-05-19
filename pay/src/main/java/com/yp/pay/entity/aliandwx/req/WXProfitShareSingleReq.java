package com.yp.pay.entity.aliandwx.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
public class WXProfitShareSingleReq {

    @NotBlank(message = "商户编号不能为空")
    @ApiModelProperty(value = "商户编号（统一分配）")
    private String merchantNo;

    @NotBlank(message = "渠道支付订单号不能为空")
    @ApiModelProperty("渠道支付订单号")
    private String channelOrderNo;

    @ApiModelProperty("分账接收方列表")
    private List<WXProfitShareReceiverReq> wxProfitShareReceiverReqs;

}
