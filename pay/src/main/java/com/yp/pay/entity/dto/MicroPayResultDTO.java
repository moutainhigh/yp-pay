package com.yp.pay.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 付款码支付返回实体类
 *
 * @author liuX
 * @time 2020/7/6 17:24
 *
 */
@Data
public class MicroPayResultDTO {

    @ApiModelProperty(value = "商户订单号")
    private String orderNo;

    @ApiModelProperty(value = "平台订单号")
    private String platOrderNo;

    @ApiModelProperty(value = "支付响应状态码 1：处理中 2:支付成功 3：支付失败")
    private Integer returnCode;

    @ApiModelProperty(value = "支付响应结果描述")
    private String returnMsg;
}