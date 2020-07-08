package com.yp.pay.wx.mapper;

import com.yp.pay.entity.entity.ProfitShareRecordDO;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author: liuX
 * @date: 2019.12.12 11:10
 * @description: TradePaymentRecordMapper
 */
@org.apache.ibatis.annotations.Mapper
public interface ProfitShareRecordMapper extends Mapper<ProfitShareRecordDO> {

    /**
     * 更新分账交易记录数据
     *
     * @param profitShareRecordDO
     * @return
     */
    int updateRecodeByInput(ProfitShareRecordDO profitShareRecordDO);

    /**
     * 通过Map查询分账交易数据
     * <p>
     * 可以通过商户订单号orderNo，渠道订单号channelOrderNo，记录号sysNo和订单状态status查询数据
     *
     * @param map
     * @return
     */
    List<ProfitShareRecordDO> selectRecodeByMap(Map map);

    /**
     * 通过商户订单号查询分账交易数据
     *
     * @param orderNo    商户订单号
     * @param merchantNo 商户号
     * @return
     */
    List<ProfitShareRecordDO> selectRecodeByOrderNo(@Param(value = "orderNo") String orderNo, @Param(value = "merchantNo") String merchantNo);

    /**
     * 通过平台订单号查询分账交易数据
     *
     * @param platOrderNo 平台订单号
     * @param merchantNo  商户号
     * @return
     */
    List<ProfitShareRecordDO> selectRecodeByPlatOrderNo(@Param(value = "platOrderNo") String platOrderNo, @Param(value = "merchantNo") String merchantNo);

    /**
     * 通过渠道订单号查询分账交易数据
     *
     * @param channelOrderNo 渠道订单号
     * @param merchantNo     商户号
     * @return
     */
    List<ProfitShareRecordDO> selectRecodeByChannelOrderNo(@Param(value = "channelOrderNo") String channelOrderNo, @Param(value = "merchantNo") String merchantNo);

    /**
     * 通过商户分账单号查询分账交易数据
     *
     * @param profitShareNo 商户分账单号
     * @param merchantNo    商户号
     * @return
     */
    ProfitShareRecordDO selectRecodeByProfitShareNo(@Param(value = "profitShareNo") String profitShareNo, @Param(value = "merchantNo") String merchantNo);

    /**
     * 通过平台分账单号查询分账交易数据
     *
     * @param platProfitShareNo 平台分账单号
     * @param merchantNo        商户号
     * @return
     */
    ProfitShareRecordDO selectRecodeByPlatProfitShareNo(@Param(value = "platProfitShareNo") String platProfitShareNo, @Param(value = "merchantNo") String merchantNo);

    /**
     * 查询分账交易记录
     *
     * @param profitShareRecordDO
     * @return
     */
    ProfitShareRecordDO selectRecodeByEntity(ProfitShareRecordDO profitShareRecordDO);

    /**
     * 查询分账交易记录集合
     *
     * @param profitShareRecordDO
     * @return
     */
    List<ProfitShareRecordDO> selectRecodeListByEntity(ProfitShareRecordDO profitShareRecordDO);

}
