package com.yp.pay.wx.entity.dao;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Data
@Table(name = "merchant_pay_info")
public class MerchantPayInfoDO {
    @Id
    private Long sysno;

    private String merchantNo;

    private String merchantName;

    private Long customerSysno;

    private String appId;

    private String partnerId;

    private String partnerKey;

    private String payWayCode;

    private String rsaPrivateKey;

    private String rsaPublicKey;

    private String aliPublicKey;

    private String subMerchantId;

    private String payRate;

    private String certPath;

    private Integer status;

    private BigDecimal maxOrderAmount;

    private String returnUrl;

    private String quitUrl;

    private String notifyUrl;

    private String merNotifyUrl;

    private String merRefundNotifyUrl;
}