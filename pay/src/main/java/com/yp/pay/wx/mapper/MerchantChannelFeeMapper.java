package com.yp.pay.wx.mapper;

import com.yp.pay.entity.entity.MerchantChannelFeeDO;
import tk.mybatis.mapper.common.Mapper;

import java.util.Map;

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

    /**
     * 通过数据参数查询商户对应支付渠道费率配置
     *
     * @param map 输入参数包括 sysNo、merchantSysNo、payWayCode、payTypeCode、status
     * @return
     */
    MerchantChannelFeeDO selectByMap(Map<String,Object> map);


}
