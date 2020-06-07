package com.yp.pay.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Table;
import java.util.Date;

/**
 * @description: 商户退款记录实体类
 *
 * @author: liuX
 * @time: 2020/6/7 22:40
 */
@Data
@Table(name = "trade_refund_record")
public class TradeRefundRecordDTO {

    @ApiModelProperty(value = "记录号")
    private Long sysNo;

    @ApiModelProperty(value = "商户编号")
    private String merchantNo;

    @ApiModelProperty(value = "商户名称")
    private String merchantName;

    @ApiModelProperty(value = "商户订单号")
    private String orderNo;

    @ApiModelProperty(value = "平台支付订单号")
    private String platOrderNo;

    @ApiModelProperty(value = "渠道订单号")
    private String channelOrderNo;

    @ApiModelProperty(value = "订单金额（单位为分）")
    private Integer orderAmount;

    @ApiModelProperty(value = "支付渠道编码(WX_PAY:微信支付 ALI_PAY:支付宝支付)")
    private String payWayCode;

    @ApiModelProperty(value = "商户退款订单号")
    private String refundNo;

    @ApiModelProperty(value = "平台退款订单号")
    private String platRefundNo;

    @ApiModelProperty(value = "渠道退款订单号")
    private String channelRefundNo;

    @ApiModelProperty(value = "状态（0已申请，1退款中，2退款成功，3退款失败）")
    private Integer status;

    @ApiModelProperty(value = "错误码")
    private String errCode;

    @ApiModelProperty(value = "错误描述")
    private String errCodeDes;

    @ApiModelProperty(value = "申请退款时间")
    private Date refundApplyTime;

    @ApiModelProperty(value = "退款完成时间")
    private Date refundSuccessTime;

    @ApiModelProperty(value = "申请退款金额（单位为分）")
    private Integer refundApplyAmount;

    @ApiModelProperty(value = "成功退款金额（单位为分）")
    private Integer successRefundAmount;

    @ApiModelProperty(value = "订单来源")
    private String orderFrom;

    @ApiModelProperty(value = "支付类型编码")
    private String payTypeCode;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "商户手续费（单位为分）")
    private Integer merCost;

}