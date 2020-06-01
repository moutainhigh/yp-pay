package com.yp.pay.entity.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @description: 订单查询请求实体类
 *
 * @author: liuX
 * @time: 2020/5/31 8:56
 */
@Data
public class WxOrderQueryOrReverseReq {

    @NotBlank(message = "商户编号不能为空")
    @ApiModelProperty(value = "商户编号（统一分配）",required = true)
    private String merchantNo;

    @ApiModelProperty(value = "商户订单号(和平台订单号选填一项)")
    private String orderNo;

    @ApiModelProperty(value = "平台订单号(和商户订单号选填一项)")
    private String platOrderNo;

    @ApiModelProperty(value = "平台订单号(和商户订单号选填一项)",hidden = true)
    private String channelOrderNo;

}
