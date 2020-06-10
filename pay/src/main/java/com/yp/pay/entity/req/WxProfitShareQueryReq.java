package com.yp.pay.entity.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @description: 分账结果查询请求实体类
 *
 * @author: liuX
 * @time: 2020/6/8 19:46
 */
@Data
public class WxProfitShareQueryReq {

    @NotBlank(message = "商户编号不能为空")
    @ApiModelProperty(value = "商户编号（统一分配）")
    private String merchantNo;

    @NotBlank(message = "商户支付订单号不能为空")
    @ApiModelProperty("商户支付订单号")
    private String orderNo;

    @ApiModelProperty(value = "平台支付订单号",hidden = true)
    private String platOrderNo;

    @NotBlank(message = "分账单号不能为空")
    @ApiModelProperty("分账单号")
    private String profitShareNo;

    @ApiModelProperty(value = "平台分账单号",hidden = true)
    private String platProfitShareNo;

}
