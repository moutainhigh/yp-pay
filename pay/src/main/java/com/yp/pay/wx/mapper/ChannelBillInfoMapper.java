package com.yp.pay.wx.mapper;

import com.yp.pay.wx.entity.dao.ChannelBillInfoDO;
import com.yp.pay.wx.entity.dao.TradePaymentRecordDO;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author: liuX
 * @date: 20200304 17:31
 * @description: ChannelBillInfoMapper
 */
public interface ChannelBillInfoMapper extends Mapper<TradePaymentRecordDO>{

    int batchInsertChannelBillInfo(@Param("list") List<ChannelBillInfoDO> list);

    List<ChannelBillInfoDO> selectChannelBillInfo(Map<String, Object> inputDate);

}
