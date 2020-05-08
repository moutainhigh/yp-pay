package com.yp.pay.entity.aliandwx.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class WXDownloadBillReq {

    @NotBlank(message = "商户编号不能为空")
    @ApiModelProperty("商户编号（统一分配）")
    private String merchantNo;

    @NotBlank(message = "资金账单日期不能为空")
    @ApiModelProperty("资金账单日期")
    private String billDate;

}
