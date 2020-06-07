package com.yp.pay.entity.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @description: 请求微信单次分账中receivers对象，该对象由WxProfitShareReceiverReq转化而来
 *
 * @author: liuX
 * @time: 2020/5/24 23:36
 */
@Data
public class WxProfitShareToWxReq {

    @ApiModelProperty(value = "分账接收方类型（1：商户ID；2：个人微信号；3：个人openid）")
    private String type;

    @ApiModelProperty("分账接收方账号（类型是1时，是商户ID；类型是2时，是个人微信号；类型是3时，是个人openid）")
    private String account;

    @ApiModelProperty("分账金额（单位为分，只能为整数）")
    private Integer amount;

    @ApiModelProperty("分账描述")
    private String description;
}
