package com.yp.pay.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @description: 返回调用端的交易记录实体对象
 *
 * @author: liuX
 * @time: 2020/5/29 10:05
 */
@Data
public class TradePaymentRecordDTO {

    @ApiModelProperty(value = "记录号")
    private Long sysNo;

    @ApiModelProperty(value = "商户号")
    private String merchantNo;

    @ApiModelProperty(value = "商户名称")
    private String merchantName;

    @ApiModelProperty(value = "商品名")
    private String productName;

    @ApiModelProperty(value = "商户订单号")
    private String orderNo;

    @ApiModelProperty(value = "平台订单号")
    private String platOrderNo;

    @ApiModelProperty(value = "渠道订单号")
    private String channelOrderNo;

    @ApiModelProperty(value = "订单来源ip")
    private String orderIp;

    @ApiModelProperty(value = "订单来源链接")
    private String orderRefererUrl;

    @ApiModelProperty(value = "订单金额")
    private BigDecimal orderAmount;

    @ApiModelProperty(value = "分账标示（0：不分账 1：分账）")
    private Integer profitShareSign;

    @ApiModelProperty(value = "分账状态（0：未分账 1：部分分账 2：完结分账 3：分账回退）")
    private Integer profitShareStatus;

    @ApiModelProperty(value = "支付渠道编码(WX_PAY:微信支付 ALI_PAY:支付宝支付)")
    private String payWayCode;

    @ApiModelProperty(value = "状态（0已提交,1处理中，2交易成功，3交易失败，4已关闭）")
    private Integer status;

    @ApiModelProperty(value = "错误码")
    private String errCode;

    @ApiModelProperty(value = "错误描述")
    private String errCodeDes;

    @ApiModelProperty(value = "支付成功时间")
    private Date paySuccessTime;

    @ApiModelProperty(value = "退款状态（0：未退款，1：部分退款，2：全额退款）")
    private Integer refundStatus;

    @ApiModelProperty(value = "退款次数")
    private Integer refundTimes;

    @ApiModelProperty(value = "支付类型编码")
    private String payTypeCode;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "商户手续费（单位为分）")
    private Integer merCost;

    @ApiModelProperty(value = "订单详情")
    private String tradeDetail;

    @ApiModelProperty(value = "订单附加数据")
    private String tradeAttach;

    @ApiModelProperty(value = "二维码是否失效（0有效, 1失效）")
    private Integer qrCodeStatus;
}