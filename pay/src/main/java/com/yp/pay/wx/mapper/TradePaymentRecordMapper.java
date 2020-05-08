package com.yp.pay.wx.mapper;

import com.yp.pay.entity.aliandwx.dao.TradePaymentRecordDO;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author: lijiang
 * @date: 2019.12.12 11:10
 * @description: TradePaymentRecordMapper
 */
@org.apache.ibatis.annotations.Mapper
public interface TradePaymentRecordMapper extends Mapper<TradePaymentRecordDO>{

    int updateRecodeByInput(TradePaymentRecordDO tradePaymentRecordDO);
}
