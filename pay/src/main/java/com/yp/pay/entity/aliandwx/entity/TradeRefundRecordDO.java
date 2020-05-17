package com.yp.pay.entity.aliandwx.entity;

import lombok.Data;

import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;


@Data
@Table(name = "trade_refund_record")
public class TradeRefundRecordDO {

    private Long sysNo;

    private String merchantNo;

    private String merchantName;

    private Integer version;

    private String merchantOrderNo;

    private String channelOrderNo;

    private BigDecimal orderAmount;

    /**
     * 支付渠道编码(WX_PAY:微信支付 ALI_PAY:支付宝支付)
     */
    private String payWayCode;

    private String refundOrderNo;

    private String channelRefundOrderNo;

    private Integer status;

    private String errCode;

    private String errCodeDes;

    private Date refundApplyTime;

    private Date refundSuccessTime;

    private BigDecimal refundApplyAmount;

    private BigDecimal successRefundAmount;

    private String orderFrom;

    private String payTypeCode;

    private String remark;

    private BigDecimal merCost;

    private String createUser;

    private Date createDate;

    private String modifyUser;

    private Date modifyDate;
}