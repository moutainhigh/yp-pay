package com.yp.pay.wx.mapper;

import com.yp.pay.entity.aliandwx.entity.TradePaymentRecordDO;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author: liuX
 * @date: 2019.12.12 11:10
 * @description: TradePaymentRecordMapper
 */
@org.apache.ibatis.annotations.Mapper
public interface TradePaymentRecordMapper extends Mapper<TradePaymentRecordDO>{

    /**
     * 更新支付订单数据
     * @param tradePaymentRecordDO
     * @return
     */
    int updateRecodeByInput(TradePaymentRecordDO tradePaymentRecordDO);
}
