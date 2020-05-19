package com.yp.pay.wx.service;

import com.yp.pay.base.exception.BusinessException;
import com.yp.pay.entity.aliandwx.dto.WXProfitShareQueryDTO;
import com.yp.pay.entity.aliandwx.dto.WXProfitShareReceiverAddDTO;
import com.yp.pay.entity.aliandwx.dto.WXProfitShareReturnDTO;
import com.yp.pay.entity.aliandwx.dto.WXSingleProfitShareDTO;
import com.yp.pay.entity.aliandwx.req.*;

public interface WXProfitShareService {

    /**
     * 单次分账（分账后剩余资金会自动解冻到商户账户，不需要调用完结分账接口）
     * @return WXSingleProfitShareDTO
     * @throws BusinessException
     */
    WXSingleProfitShareDTO singleProfitShare(WXProfitShareSingleReq wxProfitShareSingleReq) throws BusinessException;


    /**
     * 多次分账（因为是多次分账，所以不知道什么时候完成分账，所以业务认为分账完成后需要调用完结分账接口）
     * @return
     * @throws BusinessException
     */
    WXSingleProfitShareDTO multiProfitShare(WXProfitShareSingleReq wxProfitShareSingleReq) throws BusinessException;

    /**
     * 查询分账结果
     * @return
     * @throws BusinessException
     */
    WXProfitShareQueryDTO profitShareQuery(WXProfitShareQueryReq wxProfitShareQueryReq) throws BusinessException;

    /**
     * 添加分账接收方
     * @return
     * @throws BusinessException
     */
    WXProfitShareReceiverAddDTO profitShareAddReceiver(WXProfitShareReceiverAddReq wxProfitShareReceiverAddReq) throws BusinessException;

    /**
     * 删除分账接收方
     * @return
     * @throws BusinessException
     */
    WXProfitShareReceiverAddDTO profitShareRemoveReceiver(WXProfitShareReceiverSimpleReq wxProfitShareReceiverSimpleReq) throws BusinessException;

    /**
     * 完結分账
     * @return
     * @throws BusinessException
     */
    WXSingleProfitShareDTO profitShareFinish(WXProfitShareFinishReq wxProfitShareFinishReq) throws BusinessException;

    /**
     * 分账回退
     *
     * 在分账完成后，但是订单需要退款，这个时候就需要分账回退，然后在将商户的钱退款到用户账户
     * @return
     * @throws BusinessException
     */
    WXProfitShareReturnDTO profitShareReturn(WXProfitShareReturnReq wxProfitShareReturnReq) throws BusinessException;

    /**
     * 分账回退结果查询
     * @return
     * @throws BusinessException
     */
    WXProfitShareReturnDTO profitShareReturnQuery(WXProfitShareReturnQueryReq wxProfitShareReturnQueryReq) throws BusinessException;

}
