package com.yp.pay.entity.entity;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Table(name = "trade_payment_order")
public class TradePaymentOrderDO {
    @Id
    private Long sysno;

    private Integer version;

    private String createUser;

    private Date createTime;

    private String modifyUser;

    private Date modifyTime;

    private Integer status;

    private String productName;

    private String merchantOrderNo;

    private BigDecimal orderAmount;

    private String orderFrom;

    private String merchantName;

    private String merchantNo;

    private Date orderTime;

    private String orderIp;

    private String returnUrl;

    private String notifyUrl;

    private String cancelReason;

    private Date expireTime;

    private String payWayCode;

    private String remark;

    private String trxType;

    private String trxNo;

    private String payTypeCode;

    private String isRefund;

    private Integer refundTimes;

    private BigDecimal successRefundAmount;

}