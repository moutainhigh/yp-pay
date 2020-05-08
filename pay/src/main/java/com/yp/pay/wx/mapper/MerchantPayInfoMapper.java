package com.yp.pay.wx.mapper;


import com.yp.pay.entity.aliandwx.dao.MerchantPayInfoDO;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author: lijiang
 * @date: 2019.12.12 10:38
 * @description: MerchantPayInfoMapper
 */
@org.apache.ibatis.annotations.Mapper
public interface MerchantPayInfoMapper extends Mapper<MerchantPayInfoDO> {

    List<MerchantPayInfoDO> selectMerchantInfo(Map<String, Object> inputDate);
}
