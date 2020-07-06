package com.yp.pay.entity.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @description: 完结分账商户调用请求实体类
 *
 * @author: liuX
 * @time: 2020/6/14 16:16
 */
@Data
public class WxProfitShareFinishReq extends CommonReq {

    @NotBlank(message = "商户订单号不能为空")
    @ApiModelProperty(value = "商户订单号")
    private String orderNo;

    @NotBlank(message = "商户分账订单号不能为空")
    @ApiModelProperty(value = "商户分账订单号")
    private String 	profitShareNo;

    @NotBlank(message = "分账完结描述不能为空")
    @ApiModelProperty(value = "分账完结描述")
    private String description;
}
