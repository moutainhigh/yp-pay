package com.jd.pay.bindCard.entity.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class BindCardInfoReq {

    @NotBlank(message = "用户编号不能为空")
    @ApiModelProperty(value = "用户编号（唯一）")
    public String userCode;

    @NotBlank(message = "合作机构渠道id不能为空 YILLION：亿联银行 CQFMB：富民银行")
    @ApiModelProperty(value = "合作机构渠道id")
    public String channelId;

    @NotBlank(message = "用户开户账号不能为空")
    @ApiModelProperty(value = "用户开户账号")
    public String userBankCode;

    @NotBlank(message = "银行卡账户类型不能为空 （D: 借记卡 [对私] C: 贷记卡 [对私] SC: 准贷记卡 [对私] BDA: 基本户 [对公] GDA: 一般户 [对公]）")
    @ApiModelProperty(value = "银行卡账户类型")
    public String bankAccountType;

    @NotBlank(message = "银行账号不能为空")
    @ApiModelProperty(value = "银行账号")
    public String bankCardNo;

    @NotBlank(message = "账户名字不能为空")
    @ApiModelProperty(value = "账户名字")
    public String bankCardAccountName;

    @NotBlank(message = "银行名字不能为空")
    @ApiModelProperty(value = "银行名字")
    public String bankName;

    @NotBlank(message = "开户银行的编码不能为空（如：ICBC-工商银行）")
    @ApiModelProperty(value = "开户银行的编码")
    public String bankCode;

    @NotBlank(message = "开户支行联行号不能为空")
    @ApiModelProperty(value = "开户支行联行号")
    public String acctUnionBankCode;

    @NotBlank(message = "开户支行名称不能为空")
    @ApiModelProperty(value = "开户支行名称")
    public String acctSubBankName;

    @NotBlank(message = "银行预留手机号不能为空")
    @ApiModelProperty(value = "银行预留手机号")
    public String acctMobile;

    @ApiModelProperty(value = "备注")
    public String remark;

}
