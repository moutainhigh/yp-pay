package com.yp.pay.entity.aliandwx.req;

import lombok.Data;

/**
 * @author: lijiang
 * @date: 2019.12.11 14:54
 * @description: PlatGoodsDetail
 */
@Data
public class PlatGoodsDetail {

    public PlatGoodsDetail(){

    }
    /** 构造函数 传入所需参数 **/
    public PlatGoodsDetail(String skuId , String skuName , Long singlePrice , Integer nums){
        this.skuId = skuId;
        this.skuName = skuName;
        this.singlePrice = singlePrice;
        this.nums = nums;
    }

    /** 商品ID **/
    private String skuId;

    /** 名称 **/
    private String skuName;

    /** 单价 **/
    private Long singlePrice;

    /** 数量 **/
    private Integer nums;
}
