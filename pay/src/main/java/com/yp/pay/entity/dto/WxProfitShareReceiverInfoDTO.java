package com.yp.pay.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class WxProfitShareReceiverInfoDTO {

    @ApiModelProperty(value = "分账接收方类型",notes = "分账接收方类型（1:商户ID；2:个人微信号；3:个人openid）")
    private String receiverType;

    @ApiModelProperty(value = "分账接收方账号")
    private String 	receiverAccount;

    @ApiModelProperty(value = "分账金额",notes = "单位为分，只能为整数")
    private Integer amount;

    @ApiModelProperty(value = "分账描述")
    private String description;

    @ApiModelProperty(value = "分账结果",notes = "状态（0处理中，1分账成功，2分账失败，3已回退）")
    private String status;

    @ApiModelProperty(value = "分账完成时间")
    private Date successTime;

    @ApiModelProperty(value = "分账失败原因")
    private String failReason;
}
