package com.yp.pay.wx.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ScanCodeDTO {

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "预支付交易会话标识(微信生成的预支付会话标识，用于后续接口调用中使用，该值有效期为2小时)")
    private String prepayId;

    @ApiModelProperty(value = "二维码链接")
    private String codeUrl;
}