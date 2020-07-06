package com.yp.pay.entity.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @description: 对账单下载请求实体类
 *
 * @author: liuX
 * @time: 2020/6/10 10:59
 * @params:
 * @return:
 */
@Data
public class WxDownloadBillReq extends CommonReq{

    @NotBlank(message = "资金账单日期不能为空")
    @ApiModelProperty("资金账单日期")
    private String billDate;

}
