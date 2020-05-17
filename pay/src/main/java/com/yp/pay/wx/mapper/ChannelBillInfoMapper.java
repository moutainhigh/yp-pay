package com.yp.pay.wx.mapper;

import com.yp.pay.entity.aliandwx.entity.ChannelBillInfoDO;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author: liuX
 * @date: 20200304 17:31
 * @description: ChannelBillInfoMapper
 */
@org.apache.ibatis.annotations.Mapper
public interface ChannelBillInfoMapper extends Mapper<ChannelBillInfoDO>{

    /**
     * 批量查询订单对账数据
     * @param list
     * @return
     */
    int batchInsertChannelBillInfo(@Param("list") List<ChannelBillInfoDO> list);

    /**
     * 查询指定订单对账数据
     * @param inputDate
     * @return
     */
    List<ChannelBillInfoDO> selectChannelBillInfo(Map<String, Object> inputDate);

}
