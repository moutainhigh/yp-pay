package com.yp.pay.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UnionPayCodeDTO {

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "二维码链接")
    private String qrCodeUrl;
}