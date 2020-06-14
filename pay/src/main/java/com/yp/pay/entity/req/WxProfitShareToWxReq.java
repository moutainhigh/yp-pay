package com.yp.pay.entity.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @description: 请求微信单次分账中receivers对象，该对象由WxProfitShareReceiverReq转化而来
 *
 * @author: liuX
 * @time: 2020/5/24 23:36
 */
@Data
public class WxProfitShareToWxReq {

    /**
     * 该对象为请求微信方的对象，所有类型应该是微信需要的类型
     */
    @ApiModelProperty(value = "分账接收方类型（MERCHANT_ID:商户ID; PERSONAL_WECHATID:个人微信号; PERSONAL_OPENID：个人openid）")
    private String type;

    @ApiModelProperty("分账接收方账号（类型是MERCHANT_ID时，是商户ID；类型是PERSONAL_WECHATID时，是个人微信号；类型是PERSONAL_OPENID时，是个人openid）")
    private String account;

    @ApiModelProperty("分账金额（单位为分，只能为整数）")
    private Integer amount;

    @ApiModelProperty("分账描述")
    private String description;
}
