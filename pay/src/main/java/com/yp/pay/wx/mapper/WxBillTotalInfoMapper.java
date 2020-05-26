package com.yp.pay.wx.mapper;

import com.yp.pay.entity.entity.MerchantChannelFeeDO;
import com.yp.pay.entity.entity.WxBillTotalInfoDO;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author: liuX
 * @date: 2019.12.12 11:10
 * @description: TradePaymentRecordMapper
 */
@org.apache.ibatis.annotations.Mapper
public interface WxBillTotalInfoMapper extends Mapper<WxBillTotalInfoDO>{

    /**
     * 更新支付订单数据
     * @param wxBillTotalInfoDO
     * @return
     */
    int updateRecodeBySysNo(WxBillTotalInfoDO wxBillTotalInfoDO);

    /**
     * 查询指定订单对账数据
     * @param inputDate
     * @return
     */
    List<WxBillTotalInfoDO> selectChannelBillInfo(Map<String, Object> inputDate);
}
