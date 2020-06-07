package com.yp.pay.wx.mapper;

import com.yp.pay.entity.entity.ProfitShareReceiverDO;
import tk.mybatis.mapper.common.Mapper;

import java.util.Map;

/**
 * @author: liuX
 * @date: 20200304 17:31
 * @description: ChannelBillInfoMapper
 */
@org.apache.ibatis.annotations.Mapper
public interface ProfitShareReceiverMapper extends Mapper<ProfitShareReceiverDO>{

    /**
     * 通过map查询分账方信息
     *
     * @author: liuX
     * @time: 2020/6/7 21:27
     * @params: map
     * @return:
     */
    ProfitShareReceiverDO selectReceiverByMap(Map<String,Object> map);

    /**
     * @description: 通过实体类对象参数查询分账方信息
     *
     * @author: liuX
     * @time: 2020/6/7 21:48
     * @params: profitShareReceiverDO
     * @return: ProfitShareReceiverDO
     */
    ProfitShareReceiverDO selectReceiverByEntity(ProfitShareReceiverDO profitShareReceiverDO);
}
