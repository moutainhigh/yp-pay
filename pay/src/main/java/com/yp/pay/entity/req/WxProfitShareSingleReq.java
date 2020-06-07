package com.yp.pay.entity.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @description: 单次分账请求实体类
 *
 * @author: liuX
 * @time: 2020/5/28 15:07
 */
@Data
public class WxProfitShareSingleReq {

    @NotBlank(message = "商户编号不能为空")
    @ApiModelProperty(value = "商户编号（统一分配）")
    private String merchantNo;

    @NotBlank(message = "分账单号不能为空，且必须唯一")
    @ApiModelProperty("分账单号")
    private String profitShareNo;

    @ApiModelProperty("平台支付订单号")
    private String platOrderNo;

    @ApiModelProperty("商户支付订单号")
    private String orderNo;

    @ApiModelProperty("分账接收方列表")
    private List<WxProfitShareReceiverReq> wxProfitShareReceiverReqs;

}
