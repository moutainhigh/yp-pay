package com.yp.pay.wx.mapper;

import com.yp.pay.entity.aliandwx.entity.TradeRefundRecordDO;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author: liuX
 * @date: 2019.12.12 11:10
 * @description: TradePaymentRecordMapper
 */
@org.apache.ibatis.annotations.Mapper
public interface TradeRefundRecordMapper extends Mapper<TradeRefundRecordDO>{

    /**
     * 更新支付订单数据
     * @param tradeRefundRecordDO
     * @return
     */
    int updateRecodeByInput(TradeRefundRecordDO tradeRefundRecordDO);
}
