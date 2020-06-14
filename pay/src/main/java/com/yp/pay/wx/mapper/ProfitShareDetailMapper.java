package com.yp.pay.wx.mapper;

import com.yp.pay.entity.entity.ProfitShareDetailDO;
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
public interface ProfitShareDetailMapper extends Mapper<ProfitShareDetailDO>{

    /**
     * 更新分账详情数据
     * @param profitShareRecordDO
     * @return
     */
    int updateDetailByInput(ProfitShareDetailDO profitShareRecordDO);

    /**
     * 通过分账单号更新多条分账详情数据
     * @param profitShareRecordDO
     * @return
     */
    int updateDetailByPlatShareNo(ProfitShareDetailDO profitShareRecordDO);
    
    /**
     * 通过记录号跟新分账详情数据
     * @param profitShareRecordDO
     * @return
     */
    int updateDetailBySysNo(ProfitShareDetailDO profitShareRecordDO);

    /**
     * 通过Map查询分账详情数据
     *
     * 可以通过商户订单号orderNo，渠道订单号channelOrderNo，记录号sysNo和订单状态status查询数据
     * @param map
     * @return
     */
    List<ProfitShareDetailDO> selectDetailByMap(Map map);

    /**
     * 通过商户订单号查询分账详情数据
     *
     * @param merchantNo 商户编号
     * @return
     */
    List<ProfitShareDetailDO> selectDetailByMerchantNo(@Param(value = "merchantNo") String merchantNo);

    /**
     * 通过平台分账单号查询分账详情数据
     *
     * @param platProfitShareNo 平台分账单号
     * @return
     */
    List<ProfitShareDetailDO> selectDetailByPlatProfitShareNo(@Param(value = "platProfitShareNo") String platProfitShareNo);

    /**
     * 查询分账详情
     *
     * @param profitShareDetailDO
     * @return
     */
    ProfitShareDetailDO selectDetailByEntity(ProfitShareDetailDO profitShareDetailDO);

    /**
     * 查询分账详情集合
     *
     * @param profitShareDetailDO
     * @return
     */
    List<ProfitShareDetailDO> selectDetailListByEntity(ProfitShareDetailDO profitShareDetailDO);

}
