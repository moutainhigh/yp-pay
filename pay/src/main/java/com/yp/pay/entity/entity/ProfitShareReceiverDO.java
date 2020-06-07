package com.yp.pay.entity.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @description: 分账接收方实体对象
 *
 * @author: liuX
 * @time: 2020/6/7 21:17
 */
@Data
@Table(name = "profit_share_receiver")
public class ProfitShareReceiverDO {
    @Id
    @ApiModelProperty(value = "id")
    private Long sysNo;

    @ApiModelProperty(value = "分账接收方类型（1:商户ID；2:个人微信号；3:个人openid）")
    private Integer receiverType;

    @ApiModelProperty(value = "分账接收方账号")
    private String receiverAccount;

    @ApiModelProperty(value = "分账接收方全称")
    private String receiverName;

    @ApiModelProperty(value = "所属商户号")
    private String merchantNo;

    @ApiModelProperty(value = "与分账方的关系类型（0:服务商; 1:门店; 2:员工; 3:店主; " +
            "4:合作伙伴; 5:总部; 6:品牌方; 7:分销商; 8:用户; 9:供应商; 10:自定义）")
    private Integer relationType;

    @ApiModelProperty(value = "与商户的自定义关系")
    private String customRelation;

    @ApiModelProperty(value = "状态（0已提交; 1添加成功; 2已删除）")
    private Integer status;

    @ApiModelProperty(value = "支付渠道 微信WX_PAY 支付宝ALI_PAY")
    private String payWayCode;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private Date createDate;

    @ApiModelProperty(value = "修改人")
    private Integer modifyUser;

    @ApiModelProperty(value = "修改时间")
    private Date modifyDate;

}