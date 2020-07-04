package com.yp.pay.entity.entity;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name = "sub_merchant_config")
public class SubMerchantConfigDO {
    @Id
    private Long sysNo;

    private Long merchantSysNo;

    private String subMerchantNo;

    private String subMerchantName;

    private String registerCode;

    private String checkCode;

    /**
     * 商户平台状态 1正常 0冻结
     */
    private Integer status;

    private Integer paymentCodeStatus;

    private Integer jsapiPayStatus;

    private Integer nativePayStatus;

    private Integer appPayStatus;

    private String comeFrom;

    private String remark;


}