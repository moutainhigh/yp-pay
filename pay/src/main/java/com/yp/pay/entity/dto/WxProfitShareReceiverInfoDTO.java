package com.yp.pay.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class WxProfitShareReceiverInfoDTO {

    @ApiModelProperty(value = "分账接收方类型",notes = "MERCHANT_ID：商户ID;PERSONAL_WECHATID：个人微信名;PERSONAL_OPENID：个人openid（由父商户APPID转换得到）;PERSONAL_SUB_OPENID: 个人sub_openid（由子商户APPID转换得到）")
    private String type;

    @ApiModelProperty(value = "分账接收方账号",notes = "类型是MERCHANT_ID时，是商户ID;类型是PERSONAL_WECHATID时，是个人微信名;类型是PERSONAL_OPENID时，是个人openid;类型是PERSONAL_SUB_OPENID时，是个人sub_openid")
    private String 	account;

    @ApiModelProperty(value = "分账金额",notes = "单位为分，只能为整数")
    private Integer amount;

    @ApiModelProperty(value = "分账描述")
    private String description;

    @ApiModelProperty(value = "分账结果",notes = "PENDING:待分账;SUCCESS:分账成功;ADJUST:分账失败待调账;RETURNED:已转回分账方;CLOSED: 已关闭")
    private String result;

    @ApiModelProperty(value = "分账完成时间")
    private Date finishTime;

    @ApiModelProperty(value = "分账失败原因")
    private String failReason;
}
