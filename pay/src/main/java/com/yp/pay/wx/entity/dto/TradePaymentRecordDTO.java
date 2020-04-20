package com.yp.pay.wx.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class TradePaymentRecordDTO {

    @ApiModelProperty(value = "记录号")
    private Long sysno;


    @ApiModelProperty(value = "状态（0已提交,1处理中，2交易成功，3交易失败，4已关闭）")
    private Integer status;

    @ApiModelProperty(value = "错误码")
    private String errCode;

    @ApiModelProperty(value = "错误描述")
    private String errCodeDes;

    @ApiModelProperty(value = "商品名")
    private String productName;

    @ApiModelProperty(value = "商户订单号")
    private String merchantOrderNo;

    @ApiModelProperty(value = "渠道订单号")
    private String channelOrderNo;

    @ApiModelProperty(value = "商户名称")
    private String merchantName;

    @ApiModelProperty(value = "商户号")
    private String merchantNo;

    @ApiModelProperty(value = "订单来源ip")
    private String orderIp;

    @ApiModelProperty(value = "订单来源链接")
    private String orderRefererUrl;

    @ApiModelProperty(value = "订单金额")
    private BigDecimal orderAmount;

    @ApiModelProperty(value = "费率")
    private BigDecimal feeRate;

    @ApiModelProperty(value = "返回url")
    private String returnUrl;

    @ApiModelProperty(value = "通知url")
    private String notifyUrl;

    @ApiModelProperty(value = "支付渠道code")
    private String payWayCode;

    @ApiModelProperty(value = "支付成功时间")
    private Date paySuccessTime;

    @ApiModelProperty(value = "订单完成时间")
    private Date completeTime;

    @ApiModelProperty(value = "是否退款")
    private String isRefund;

    @ApiModelProperty(value = "退款次数")
    private Integer refundTimes;

    @ApiModelProperty(value = "成功退款金额")
    private BigDecimal successRefundAmount;

    @ApiModelProperty(value = "订单来源")
    private String orderFrom;

    @ApiModelProperty(value = "支付类型编码")
    private String payTypeCode;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "商户手续费")
    private BigDecimal merCost;

    @ApiModelProperty(value = "商户退款订单号")
    private String refundOrderNo;

    @ApiModelProperty(value = "渠道退款订单号")
    private String channelRefundOrderNo;

    @ApiModelProperty(value = "退款完成时间")
    private Date refundSuccessTime;

    @ApiModelProperty(value = "订单详情")
    private String tradeDetail;

    @ApiModelProperty(value = "订单附加数据")
    private String tradeAttach;

    @ApiModelProperty(value = "二维码是否失效（0有效, 1失效）")
    private Integer qrcodeStatus;
}