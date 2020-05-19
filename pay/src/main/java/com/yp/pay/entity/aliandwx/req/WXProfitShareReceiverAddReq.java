package com.yp.pay.entity.aliandwx.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class WXProfitShareReceiverAddReq {

    @NotBlank(message = "分账接收方类型")
    @ApiModelProperty(value = "分账接收方类型（MERCHANT_ID：商户ID；PERSONAL_WECHATID：个人微信号；PERSONAL_OPENID：个人openid）")
    private String type;

    @NotBlank(message = "分账接收方账号")
    @ApiModelProperty("分账接收方账号（类型是MERCHANT_ID时，是商户ID；类型是PERSONAL_WECHATID时，是个人微信号；类型是PERSONAL_OPENID时，是个人openid）")
    private String account;

    @ApiModelProperty("分账接收方全称")
    private Integer name;

    @NotBlank(message = "分账金额")
    @ApiModelProperty("与分账方的关心类型（SERVICE_PROVIDER：服务商; STORE：门店; STAFF：员工; STORE_OWNER：店主; " +
            "PARTNER：合作伙伴; HEADQUARTER：总部; BRAND：品牌方; DISTRIBUTOR：分销商; USER：用户; SUPPLIER：供应商; CUSTOM：自定义）")
    private String relationType;

    @ApiModelProperty("自定义关系类型（当字段relationType的值为CUSTOM时，本字段必填）")
    private Integer customRelation;
}
