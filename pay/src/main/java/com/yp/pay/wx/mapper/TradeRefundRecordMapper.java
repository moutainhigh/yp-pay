package com.yp.pay.wx.mapper;

import com.yp.pay.entity.entity.TradeRefundRecordDO;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author: liuX
 * @date: 2019.12.12 11:10
 * @description: TradePaymentRecordMapper
 */
@org.apache.ibatis.annotations.Mapper
public interface TradeRefundRecordMapper extends Mapper<TradeRefundRecordDO>{

    /**
     * 更新退款订单数据
     * @param tradeRefundRecordDO
     * @return
     */
    int updateRefundByInput(TradeRefundRecordDO tradeRefundRecordDO);

    /**
     * 通过商户退款单号查询退款订单数据
     *
     * @author liuX
     * @time 2020/6/7 9:37
     * @param refundNo 商户退款单号
     * @return
     */
    TradeRefundRecordDO selectRefundByRefundNo(@Param(value = "refundNo") String refundNo);

    /**
     * 通过平台退款单号查询退款订单数据
     *
     * @param platRefundNo 平台退款单号
     * @return
     */
    TradeRefundRecordDO selectRefundByPlatRefundNo(@Param(value = "platRefundNo") String platRefundNo);

    /**
     * 通过实体类查询交易退款详情表记录
     *
     * @author liuX
     * @time 2020/7/8 15:22
     * @param tradeRefundRecordDO
     * @return
     *
     */
    TradeRefundRecordDO selectByEntity(TradeRefundRecordDO tradeRefundRecordDO);
}
