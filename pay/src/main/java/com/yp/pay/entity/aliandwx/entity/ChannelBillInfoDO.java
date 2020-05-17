package com.yp.pay.entity.aliandwx.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Table(name = "channel_bill_info")
public class ChannelBillInfoDO {
    @Id
    @ApiModelProperty(value = "id")
    private Long sysno;

    @ApiModelProperty(value = "批次号（推荐是用日期年月日，防止对账单重复下载）")
    private String batchNo;

    @ApiModelProperty(value = "支付渠道 微信WX_PAY 支付宝ALI_PAY")
    private String channelCode;

    @ApiModelProperty(value = "支付类型编码 微信分为JSAPI NATIVE APP")
    private String payTypeCode;

    @ApiModelProperty(value = "渠道商户号")
    private String channelMerchantNo;

    @ApiModelProperty(value = "商户订单号")
    private String merchantOrderNo;

    @ApiModelProperty(value = "渠道订单号")
    private String channelOrderNo;

    @ApiModelProperty(value = "交易时间")
    private Date tradeTime;

    @ApiModelProperty(value = "支付成功时间")
    private Date paySuccessTime;

    @ApiModelProperty(value = "用户标识（付款方）")
    private String buyerId;

    @ApiModelProperty(value = "订单金额")
    private BigDecimal orderAmount;

    @ApiModelProperty(value = "交易金额")
    private BigDecimal tradeAmount;

    @ApiModelProperty(value = "订单附加数据")
    private String tradeAttach;

    @ApiModelProperty(value = "状态（0交易成功，1已退款）")
    private Integer status;

    @ApiModelProperty(value = "商户退款订单号")
    private String refundOrderNo;

    @ApiModelProperty(value = "渠道退款订单号")
    private String channelRefundOrderNo;

    @ApiModelProperty(value = "退款金额")
    private BigDecimal refundAmount;

    @ApiModelProperty(value = "状态（0退款完成，1退款失败）")
    private Integer refundStatus;

    @ApiModelProperty(value = "渠道手续费")
    private BigDecimal channelFee;

    @ApiModelProperty(value = "费率(费率一般为千几、万几，6位小数足够)")
    private BigDecimal channelFeeRate;

}