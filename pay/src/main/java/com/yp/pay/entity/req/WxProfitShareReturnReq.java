package com.yp.pay.entity.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @description: 分账回退请求实体类
 *
 * @author: liuX
 * @time: 2020/5/24 23:35
 */
@Data
public class WxProfitShareReturnReq extends CommonReq{

    @ApiModelProperty(value = "商户分账订单号（和渠道分账单号选填一个）")
    private String 	profitShareOrderNo;

    @ApiModelProperty(value = "渠道分账单号")
    private String channelProfitShareOrderNo;

    @NotBlank(message = "商户回退单号不能为空")
    @ApiModelProperty(value = "商户回退单号")
    private String profitShareReturnOrderNo;

    @NotBlank(message = "回退方类型不能为空")
    @ApiModelProperty(value = "回退方类型（MERCHANT_ID：商户ID；暂时只支持MERCHANT_ID，即从商户接收方回退分账金额）")
    private String profitShareReturnType;

    @NotBlank(message = "回退方账号不能为空")
    @ApiModelProperty(value = "回退方账号")
    private String returnAccount;

    @NotNull(message = "回退金额不能为空")
    @ApiModelProperty(value = "回退金额")
    private Integer returnAmount;

    @NotBlank(message = "回退描述不能为空")
    @ApiModelProperty(value = "回退描述")
    private String description;

}
