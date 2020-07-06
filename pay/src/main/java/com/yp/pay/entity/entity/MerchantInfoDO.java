package com.yp.pay.entity.entity;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 商户配置表实体类
 *
 * @author liuX
 * @time 2020/7/5 10:40
 *
 */
@Data
@Table(name = "merchant_info")
public class MerchantInfoDO {
    @Id
    private Long sysNo;

    private String merchantNo;

    private String merchantName;

    private Long customerSysNo;

    private String appId;

    private String partnerId;

    private String partnerKey;

    private String payWayCode;

    private String rsaPrivateKey;

    private String rsaPublicKey;

    private String aliPublicKey;

    private String subMerchantId;

    private String certPath;

    /**
     * 商户平台状态 1正常 0冻结
     */
    private Integer status;

    /**
     * 最大支付限额
     */
    private Integer maxOrderAmount;

    /**
     * 支付平台接收渠道通知地址
     */
    private String notifyUrl;

    /**
     * 商户平台接收支付平台通知地址
     */
    private String merNotifyUrl;

    /**
     * 商户平台接收支付平台退款通知地址
     */
    private String merRefundNotifyUrl;

    private String returnUrl;

    private String quitUrl;



}