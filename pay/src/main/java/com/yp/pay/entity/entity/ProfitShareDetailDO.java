package com.yp.pay.entity.entity;

import io.swagger.annotations.ApiModelProperty;
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
@Table(name = "profit_share_detail")
public class ProfitShareDetailDO {

    private Long sysNo;

    private Long shareRecordSysNo;

    private String platProfitShareNo;

    private String merchantNo;

    @ApiModelProperty(value = "分账接收方类型（1:商户ID；2:个人微信号；3:个人openid）")
    private Integer receiverType;

    @ApiModelProperty(value = "分账接收方账号")
    private String receiverAccount;

    private Integer amount;

    private String description;

    private Integer version;

    private Integer status;

    private Date applyTime;

    private Date successTime;

    private Date returnTime;

    private String createUser;

    private Date createDate;

    private String modifyUser;

    private Date modifyDate;
}