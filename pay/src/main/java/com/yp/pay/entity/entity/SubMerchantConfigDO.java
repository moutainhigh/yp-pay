package com.yp.pay.entity.entity;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 子商户实体类
 *
 * @author liuX
 * @time 2020/7/4 23:23
 *
 */
@Data
@Table(name = "sub_merchant_config")
public class SubMerchantConfigDO {
    @Id
    private Long sysNo;

    private String merchantNo;

    private String subMerchantNo;

    private String subMerchantName;

    private String terminalNo;

    private String terminalName;

    /**
     * 商户平台状态 1正常 0冻结
     */
    private Integer status;

    private String registerCode;

    private String checkCode;

    private Integer paymentCodeStatus;

    private Integer jsapiPayStatus;

    private Integer nativePayStatus;

    private Integer appPayStatus;

    private Integer comeFrom;

    private String remark;


}