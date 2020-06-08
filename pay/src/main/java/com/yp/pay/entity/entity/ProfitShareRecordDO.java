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
@Table(name = "profit_share_record")
public class ProfitShareRecordDO {

    private Long sysNo;

    private String orderNo;

    private String platOrderNo;

    private String channelOrderNo;

    private String profitShareNo;

    private String platProfitShareNo;

    private String channelProfitShareNo;

    private String merchantNo;

    private String merchantName;

    private Integer version;

    private String receiverInfo;

    private Integer status;

    private String errCode;

    private String errCodeDes;

    private Date applyTime;

    private Date paySuccessTime;

    private Integer refundStatus;

    private Integer merCost;

    private String createUser;

    private Date createDate;

    private String modifyUser;

    private Date modifyDate;
}