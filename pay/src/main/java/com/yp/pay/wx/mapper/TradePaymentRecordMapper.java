package com.yp.pay.wx.mapper;

import com.yp.pay.entity.entity.TradePaymentRecordDO;
import tk.mybatis.mapper.common.Mapper;

import java.util.Map;

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

    /**
     * 通过Map查询支付订单数据
     *
     * 可以通过商户订单号merchantOrderNo，渠道订单号channelOrderNo，记录号sysNo和订单状态status查询唯一一条数据
     * @param map
     * @return
     */
    TradePaymentRecordDO selectRecodeByMap(Map map);

    /**
     * 通过商户订单号查询支付订单数据
     *
     * @param merchantOrderNo 商户订单号
     * @return
     */
    TradePaymentRecordDO selectRecodeByMerchantOrderNo(String merchantOrderNo);

    /**
     * 通过平台订单号查询支付订单数据
     *
     * @param platOrderNo 平台订单号
     * @return
     */
    TradePaymentRecordDO selectRecodeByPlatOrderNo(String platOrderNo);

    /**
     * 通过渠道订单号查询支付订单数据
     *
     * @param channelOrderNo 渠道订单号
     * @return
     */
    TradePaymentRecordDO selectRecodeByChannelOrderNo(String channelOrderNo);

}
