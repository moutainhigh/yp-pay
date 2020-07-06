package com.yp.pay.entity.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 添加分账接收方请求实体类
 *
 * @author liuX
 * @date 20200524 23:11
 */
@Data
public class WxProfitShareReceiverAddReq extends CommonReq {

    @NotNull(message = "分账接收方类型不能为空")
    @ApiModelProperty(value = "分账接收方类型（1:商户ID；2:个人微信号；3:个人openid）")
    private Integer receiverType;

    @NotBlank(message = "分账接收方账号不能为空")
    @ApiModelProperty("分账接收方账号（类型是1时，是商户ID；类型是2时，是个人微信号；类型是3时，是个人openid）")
    private String receiverAccount;

    @ApiModelProperty("分账接收方全称（分账接收方类型是1时，是商户全称（必传）；" +
            "分账接收方类型是2时，是个人姓名（必传）；分账接收方类型是3时，是个人姓名（选传，传则校验））")
    private String receiverName;

    @NotNull(message = "与分账方的关系类型不能为空")
    @ApiModelProperty("与分账方的关系类型（0:服务商; 1:门店; 2:员工; 3:店主; " +
            "4:合作伙伴; 5:总部; 6:品牌方; 7:分销商; 8:用户; 9:供应商; 10:自定义）")
    private Integer relationType;

    @ApiModelProperty("自定义关系类型（当字段relationType的值为10时，本字段必填）")
    private String customRelation;
}

