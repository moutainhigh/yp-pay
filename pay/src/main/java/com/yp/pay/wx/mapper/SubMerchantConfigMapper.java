package com.yp.pay.wx.mapper;


import com.yp.pay.entity.entity.SubMerchantConfigDO;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author: liuX
 * @date: 20200704 22:50
 * @description: SubMerchantConfigMapper
 */
@org.apache.ibatis.annotations.Mapper
public interface SubMerchantConfigMapper extends Mapper<SubMerchantConfigDO> {

    /**
     * 通过Map中参数查询所有商户渠道配置信息
     * @param inputDate
     * @return
     */
    List<SubMerchantConfigDO> selectSubMerchantByMap(Map<String, Object> inputDate);

    SubMerchantConfigDO selectSubMerchantConfig(SubMerchantConfigDO subMerchantConfigDO);
}
