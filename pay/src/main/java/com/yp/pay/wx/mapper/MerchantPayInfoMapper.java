package com.yp.pay.wx.mapper;


import com.yp.pay.entity.aliandwx.entity.MerchantPayInfoDO;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author: liuX
 * @date: 2019.12.12 10:38
 * @description: MerchantPayInfoMapper
 */
@org.apache.ibatis.annotations.Mapper
public interface MerchantPayInfoMapper extends Mapper<MerchantPayInfoDO> {

    /**
     * 通过Map中参数查询所有商户渠道配置信息
     * @param inputDate
     * @return
     */
    List<MerchantPayInfoDO> selectMerchantInfo(Map<String, Object> inputDate);
}
