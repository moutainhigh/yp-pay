package com.yp.pay.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class WxSingleProfitShareDTO {

    @ApiModelProperty(value = "业务结果 SUCCESS/FAIL")
    private String resultCode;

    @ApiModelProperty(value = "当resultCode为FAIL时返回错误代码")
    private String errCode;

    @ApiModelProperty(value = "当resultCode为FAIL时返回错误描述")
    private String errCodeMsg;

    @ApiModelProperty(value = "微信订单号")
    private String ChannelOrderNo;

    @ApiModelProperty(value = "商户分账单号")
    private String profitShareNo;

    @ApiModelProperty(value = "渠道分账单号")
    private String channelProfitShareNo;

}