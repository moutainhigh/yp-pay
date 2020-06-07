package com.yp.pay.entity.entity;

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
public class TradeRefundRecordDO {

    private Long sysNo;

    private String merchantNo;

    private String merchantName;

    private Integer version;

    private String orderNo;

    private String platOrderNo;

    private String channelOrderNo;

    private Integer orderAmount;

    /**
     * 支付渠道编码(WX_PAY:微信支付 ALI_PAY:支付宝支付)
     */
    private String payWayCode;

    private String refundNo;

    private String platRefundNo;

    private String channelRefundNo;

    private Integer status;

    private String errCode;

    private String errCodeDes;

    private Date refundApplyTime;

    private Date refundSuccessTime;

    private Integer refundApplyAmount;

    private Integer successRefundAmount;

    private String orderFrom;

    private String payTypeCode;

    private String remark;

    private Integer merCost;

    private String createUser;

    private Date createDate;

    private String modifyUser;

    private Date modifyDate;
}