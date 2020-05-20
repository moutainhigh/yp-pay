package com.yp.pay.entity.aliandwx.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class WxProfitShareReturnDTO {

    @ApiModelProperty(value = "业务结果 SUCCESS/FAIL")
    private String resultCode;

    @ApiModelProperty(value = "当resultCode为FAIL时返回错误代码")
    private String errCode;

    @ApiModelProperty(value = "当resultCode为FAIL时返回错误描述")
    private String errCodeMsg;

    @ApiModelProperty(value = "微信订单号")
    private String channelOrderNo;

    @ApiModelProperty(value = "商户分账单号")
    private String profitShareNo;

    @ApiModelProperty(value = "渠道分账单号")
    private String channelProfitShareNo;

    @ApiModelProperty(value = "商户回退单号")
    private String profitShareReturnNo;

    @ApiModelProperty(value = "渠道回退单号")
    private String channelProfitShareReturnNo;

    @ApiModelProperty(value = "回退方类型",notes = "MERCHANT_ID：商户ID;PERSONAL_WECHATID：个人微信名;PERSONAL_OPENID：个人openid（由父商户APPID转换得到）;PERSONAL_SUB_OPENID: 个人sub_openid（由子商户APPID转换得到）")
    private String returnAccountType;

    @ApiModelProperty(value = "回退方账号",notes = "类型是MERCHANT_ID时，是商户ID;类型是PERSONAL_WECHATID时，是个人微信名;类型是PERSONAL_OPENID时，是个人openid;类型是PERSONAL_SUB_OPENID时，是个人sub_openid")
    private String 	returnAccount;

    @ApiModelProperty(value = "回退金额",notes = "单位为分，只能为整数")
    private Integer returnAmount;

    @ApiModelProperty(value = "回退描述")
    private String description;

    @ApiModelProperty(value = "回退结果",notes = "PROCESSING:处理中；SUCCESS:已成功；FAIL: 已失败")
    private String result;

    @ApiModelProperty(value = "回退完成时间")
    private Date finishTime;

    @ApiModelProperty(value = "失败原因")
    private String failReason;





}