package com.yp.pay.entity.aliandwx.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class WXProfitShareQueryDTO {

    @ApiModelProperty(value = "业务结果 SUCCESS/FAIL")
    private String resultCode;

    @ApiModelProperty(value = "当resultCode为FAIL时返回错误代码")
    private String errCode;

    @ApiModelProperty(value = "当resultCode为FAIL时返回错误描述")
    private String errCodeMsg;

    @ApiModelProperty(value = "微信订单号")
    private String originalChannelOrderNo;

    @ApiModelProperty(value = "商户分账单号")
    private String profitShareNo;

    @ApiModelProperty(value = "渠道分账单号")
    private String channelProfitShareNo;

    @ApiModelProperty(value = "分账状态（0-受理成功；1-处理中；2处理成功；3处理失败（已关单））")
    private Integer status;

    @ApiModelProperty(value = "关单描述")
    private String closeReason;

    @ApiModelProperty(value = "分账金额")
    private Integer amount;

    @ApiModelProperty(value = "分账描述")
    private String description;

    private List<WXProfitShareReceiverInfoDTO> wxProfitShareReceiverInfoDTOS;

}