package com.yp.pay.entity.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 公共请求实体类
 *
 * @author liuX
 * @time 2020/7/5 0:12
 *
 */
@Data
public class CommonReq {

    @NotBlank(message = "商户编号不能为空")
    @ApiModelProperty("商户编号（统一分配）")
    private String merchantNo;

    @ApiModelProperty("子商户编号（统一分配，如果有则必传）")
    private String subMerchantNo;

}
