package com.yp.pay.entity.aliandwx.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class WxMicroPayReq {

    @NotBlank(message = "商户编号不能为空")
    @ApiModelProperty("商户编号（统一分配）")
    private String merchantNo;

    @ApiModelProperty("设备信息(商户自定义，如门店编号)")
    private String deviceInfo;

    @NotBlank(message = "二维码信息不能为空")
    @ApiModelProperty("二维码信息(扫码支付授权码，设备读取用户微信中的条码或者二维码信息)")
    private String authCode;

    @NotBlank(message = "商品描述不能为空")
    @ApiModelProperty("商品描述(需要按照微信格式：店名-销售商品类目(例：小张南山店-超市))")
    private String subject;

    @ApiModelProperty("商品详情")
    private String detail;

    @ApiModelProperty("附加数据(在查询API和支付通知中原样返回，该字段主要用于商户携带订单的自定义数据)")
    private String attach;

    @NotBlank(message = "订单号不能为空")
    @ApiModelProperty("订单号(商户系统内部订单号，要求32个字符内，只能是数字、大小写字母且在同一个商户号下唯一)")
    private String orderNo;

    @NotBlank(message = "终端IP不能为空")
    @ApiModelProperty("终端IP")
    private String clientIp;

    @NotNull(message = "订单金额不能为空")
    @ApiModelProperty(value = "金额(注：单位为分)")
    private Integer amount;

    /**
     * 前端不用传，通过接口区分
     */
    @ApiModelProperty(value = "是否分账",hidden = true)
    private String profitShare;
}
