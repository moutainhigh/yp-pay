package com.yp.pay.entity.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 微信关闭订单请求实体类
 *
 * @author liuX
 * @time 2020/7/5 0:15
 *
 */
@Data
public class WxCloseOrderReq extends CommonReq{

    @NotBlank(message = "商户订单号不能为空")
    @ApiModelProperty(value = "商户订单号",required = true)
    private String orderNo;

}
