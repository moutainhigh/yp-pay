package com.yp.pay.entity.entity;

import io.swagger.models.auth.In;
import lombok.Data;

import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;


@Data
@Table(name = "wx_bill_total_info")
public class WxBillTotalInfoDO {

    private Long sysNo;

    private String merchantNo;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 订单总数
     */
    private Integer billCount;

    /**
     * 应结订单总金额（单位元）
     */
    private BigDecimal totalSettlementAmount;

    /**
     * 退款总金额（单位元）
     */
    private BigDecimal totalRefund;

    /**
     * '充值券退款总金额（单位元）
     */
    private BigDecimal chargeCouponAmount;

    /**
     * 手续费总金额（单位元）
     */
    private BigDecimal totalPayFee;

    /**
     * 订单总金额（单位元）
     */
    private BigDecimal totalOrderAmount;

    /**
     * 申请退款总金额（单位元）
     */
    private BigDecimal totalApplyRefund;

    private Date createDate;

}