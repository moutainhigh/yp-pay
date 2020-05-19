package com.yp.pay.entity.aliandwx.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class WXProfitShareReceiverSimpleReq {

    @NotBlank(message = "分账接收方类型不能为空")
    @ApiModelProperty(value = "分账接收方类型",notes = "MERCHANT_ID：商户ID;PERSONAL_WECHATID：个人微信名;PERSONAL_OPENID：个人openid（由父商户APPID转换得到）;PERSONAL_SUB_OPENID: 个人sub_openid（由子商户APPID转换得到）")
    private String type;

    @NotBlank(message = "分账接收方账号不能为空")
    @ApiModelProperty(value = "分账接收方账号",notes = "类型是MERCHANT_ID时，是商户ID;类型是PERSONAL_WECHATID时，是个人微信名;类型是PERSONAL_OPENID时，是个人openid;类型是PERSONAL_SUB_OPENID时，是个人sub_openid")
    private String 	account;
}
