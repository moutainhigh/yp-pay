package com.yp.pay.entity.aliandwx.entity;

import lombok.Data;

import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;


@Data
@Table(name = "merchant_channel_fee")
public class MerchantChannelFeeDO {

    private Long sysNo;

    private String merchantSysNo;

    /**
     * 支付渠道编码(WX_PAY:微信支付 ALI_PAY:支付宝支付)
     */
    private String payWayCode;

    private String payTypeCode;

    /**
     * 商户渠道支付方式费率
     */
    private BigDecimal feeRate;

    /**
     * 商户渠道支付方式限额
     */
    private BigDecimal maxOrderAmount;


    /**
     * 支付限制策略表sysno（暂未开发）
     */
    private Long payLimitSysNo;

    /**
     * 商户配置负责人（费率配置应该是支付平台内部员工配置）
     * 非商户信息配置（微信APPID/APP密码/API秘钥/微信商户号）
     */
    private Long contactEmpSysNo;

    /**
     * 1正常，0冻结
     */
    private Integer status;

    private String createUser;

    private Date createDate;

    private String modifyUser;

    private Date modifyDate;
}