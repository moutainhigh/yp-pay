package com.yp.pay.wx.entity.dao;

import lombok.Data;

import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Table(name = "trade_payment_record")
public class TradePaymentRecordDO {
    private Long sysno;

    private Integer version;

    private Date createDate;

    private Integer status;

    private String modifyUser;

    private String createUser;

    private Date modifyDate;

    private String errCode;

    private String errCodeDes;

    private String productName;

    private String merchantOrderNo;

    private String channelOrderNo;

    private String merchantName;

    private String merchantNo;

    private String orderIp;

    private String orderRefererUrl;

    private BigDecimal orderAmount;

    private BigDecimal feeRate;

    private String returnUrl;

    private String notifyUrl;

    private String payWayCode;

    private Date paySuccessTime;

    private Date completeTime;

    private String isRefund;

    private Integer refundTimes;

    private BigDecimal successRefundAmount;

    private String orderFrom;

    private String payTypeCode;

    private String remark;

    private BigDecimal merCost;

    private String refundOrderNo;

    private String channelRefundOrderNo;

    private Date refundSuccessTime;

    private String tradeDetail;

    private String tradeAttach;

    private Integer qrcodeStatus;
}