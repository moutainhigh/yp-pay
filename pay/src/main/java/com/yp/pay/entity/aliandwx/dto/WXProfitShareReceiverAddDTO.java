package com.yp.pay.entity.aliandwx.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WXProfitShareReceiverAddDTO {

    @ApiModelProperty(value = "业务结果 SUCCESS/FAIL")
    private String resultCode;

    @ApiModelProperty(value = "当resultCode为FAIL时返回错误代码")
    private String errCode;

    @ApiModelProperty(value = "当resultCode为FAIL时返回错误描述")
    private String errCodeMsg;

    @ApiModelProperty(value = "分账接收方")
    private WXProfitShareReceiverSimpleDTO wxProfitShareReceiverSimpleDTO;

}