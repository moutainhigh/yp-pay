package com.yp.pay.entity.entity;

import lombok.Data;

import javax.persistence.Table;
import java.util.Date;

/**
 * @description: 交易记录实体类对象
 *
 * @author: liuX
 * @time: 2020/5/29 11:15
 */
@Data
@Table(name = "trade_payment_record")
public class TradePaymentRecordDO {

    private Long sysNo;

    private String merchantNo;

    private String merchantName;

    private Integer version;

    private String productName;

    private String orderNo;

    private String platOrderNo;

    private String channelOrderNo;

    private String orderIp;

    private String orderRefererUrl;

    private Integer orderAmount;

    private Integer profitShareSign;

    private Integer profitShareStatus;

    private String payWayCode;

    private Integer status;

    private String errCode;

    private String errCodeDes;

    private Date paySuccessTime;

    private Integer refundStatus;

    private Integer refundTimes;

    private String payTypeCode;

    private String remark;

    private Integer merCost;

    private String tradeDetail;

    private String tradeAttach;

    private Integer qrCodeStatus;

    private String createUser;

    private Date createDate;

    private String modifyUser;

    private Date modifyDate;
}