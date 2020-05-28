package com.yp.pay.entity.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @description: 删除分账接收方实体类
 *
 * @author: liuX
 * @time: 2020/5/28 22:53
 */
@Data
public class WxProfitShareReceiverRemoveReq {

    @NotBlank(message = "商户编号不能为空")
    @ApiModelProperty(value = "商户编号（统一分配）")
    private String merchantNo;

    @NotNull(message = "分账接收方类型不能为空")
    @ApiModelProperty(value = "分账接收方类型",notes = "1:商户ID; 2:个人微信名; 3:个人openid")
    private Integer type;

    @NotBlank(message = "分账接收方账号不能为空")
    @ApiModelProperty(value = "分账接收方账号",notes = "类型是1时，是商户ID; 类型是2时，是个人微信名; 类型是3时，是个人openid;")
    private String 	account;
}
