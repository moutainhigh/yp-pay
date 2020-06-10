package com.yp.pay.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @description: 分账结果查询返回实体类
 *
 * @author: liuX
 * @time: 2020/6/8 20:13
 */
@Data
public class WxProfitShareQueryDTO {

    @ApiModelProperty(value = "业务结果 SUCCESS/FAIL")
    private String resultCode;

    @ApiModelProperty(value = "当resultCode为FAIL时返回错误代码")
    private String errCode;

    @ApiModelProperty(value = "当resultCode为FAIL时返回错误描述")
    private String errCodeDes;

    @ApiModelProperty(value = "商户支付订单号")
    private String orderNo;

    @ApiModelProperty(value = "平台支付订单号")
    private String platOrderNo;

    @ApiModelProperty(value = "商户分账单号")
    private String profitShareNo;

    @ApiModelProperty(value = "平台分账单号")
    private String platProfitShareNo;

    @ApiModelProperty(value = "状态（0处理中，1分账成功，2分账失败）")
    private Integer status;

    @ApiModelProperty(value = "关单描述")
    private String closeReason;

    @ApiModelProperty(value = "分账金额")
    private Integer amount;

    @ApiModelProperty(value = "分账描述")
    private String description;

    private List<WxProfitShareReceiverInfoDTO> wxProfitShareReceiverInfoDTOS;

}