package com.yp.pay.wx.mapper;


import com.yp.pay.entity.entity.MerchantInfoDO;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author: liuX
 * @date: 2019.12.12 10:38
 * @description: MerchantInfoMapper
 */
@org.apache.ibatis.annotations.Mapper
public interface MerchantInfoMapper extends Mapper<MerchantInfoDO> {

    /**
     * 通过Map中参数查询所有商户渠道配置信息
     *
     * @param inputDate
     * @return
     */
    List<MerchantInfoDO> selectMerchantInfo(Map<String, Object> inputDate);

    /**
     * 通过[商户号]和[渠道代码]查询所有[正常状态]的商户渠道配置信息
     *
     * @param merchantNo 商户号
     * @param payWayCode 渠道代码
     * @return
     */
    MerchantInfoDO selectMerchantByMerchantNo(@Param(value = "merchantNo") String merchantNo,
                                                    @Param(value = "payWayCode") String payWayCode);
}
