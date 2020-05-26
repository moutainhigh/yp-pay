package com.yp.pay.entity.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @description: TODO 该请求实体类暂时不知道是否被使用，需要确认
 *
 * @author: liuX
 * @time: 2020/5/24 23:36
 */
@Data
public class WxProfitShareReceiverReq {

    @NotBlank(message = "分账接收方类型")
    @ApiModelProperty(value = "分账接收方类型（MERCHANT_ID：商户ID；PERSONAL_WECHATID：个人微信号；PERSONAL_OPENID：个人openid）")
    private String type;

    @NotBlank(message = "分账接收方账号")
    @ApiModelProperty("分账接收方账号（类型是MERCHANT_ID时，是商户ID；类型是PERSONAL_WECHATID时，是个人微信号；类型是PERSONAL_OPENID时，是个人openid）")
    private String account;

    @NotBlank(message = "分账金额")
    @ApiModelProperty("分账金额（单位为分，只能为整数）")
    private Integer amount;

    @NotBlank(message = "分账描述")
    @ApiModelProperty("分账描述")
    private String description;
}
