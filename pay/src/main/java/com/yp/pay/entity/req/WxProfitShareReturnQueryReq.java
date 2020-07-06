package com.yp.pay.entity.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 微信分账回退查询请求实体类
 *
 * @author liuX
 * @time 2020/7/5 0:19
 * @param
 * @return
 *
 */
@Data
public class WxProfitShareReturnQueryReq extends CommonReq {

    @ApiModelProperty("商户分账单号（和渠道分账单号选填一项）")
    private String profitShareOrderNo;

    @ApiModelProperty("渠道分账单号")
    private String channelProfitShareOrderNo;

    @NotBlank(message = "商户回退单号不能为空")
    @ApiModelProperty(value = "商户回退单号）")
    private String profitShareReturnOrderNo;

}
