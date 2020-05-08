package com.yp.pay.entity.aliandwx.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BillDownloadDTO {

    @ApiModelProperty(value = "业务结果 SUCCESS/FAIL")
    private String resultCode;

    @ApiModelProperty(value = "业务描述")
    private String resultMsg;

    @ApiModelProperty(value = "当resultCode为FAIL时返回错误代码")
    private String errCode;

    @ApiModelProperty(value = "当resultCode为FAIL时返回错误描述")
    private String errCodeMsg;


}