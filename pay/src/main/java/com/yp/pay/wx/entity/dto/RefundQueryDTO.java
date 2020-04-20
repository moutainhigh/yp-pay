package com.yp.pay.wx.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RefundQueryDTO {

    @ApiModelProperty(value = "业务结果 SUCCESS/FAIL")
    private String resultCode;

    @ApiModelProperty(value = "当resultCode为FAIL时返回错误代码")
    private String errCode;

    @ApiModelProperty(value = "当resultCode为FAIL时返回错误描述")
    private String errCodeMsg;

    @ApiModelProperty(value = "微信订单号")
    private String originalChannelOrderNo;

    @ApiModelProperty(value = "商户订单号")
    private String originalOrderNo;

    @ApiModelProperty(value = "订单金额")
    private BigDecimal totalFee;

    @ApiModelProperty(value = "现金支付金额")
    private BigDecimal cashFee;

//    @ApiModelProperty(value = "退款笔数(当前返回退款笔数)",notes = "和明细想关联")
//    private String refundCount;

    private List<RefundQueryDetailDTO> refundQueryDetailDTOS;

}