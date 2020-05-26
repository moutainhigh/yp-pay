package com.yp.pay.wx.mapper;

import com.yp.pay.entity.entity.MerchantChannelFeeDO;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author: liuX
 * @date: 2019.12.12 11:10
 * @description: TradePaymentRecordMapper
 */
@org.apache.ibatis.annotations.Mapper
public interface MerchantChannelFeeMapper extends Mapper<MerchantChannelFeeDO>{

    /**
     * 更新支付订单数据
     * @param merchantChannelFeeDO
     * @return
     */
    int updateRecodeByInput(MerchantChannelFeeDO merchantChannelFeeDO);
}
