package com.yp.pay.wx.service;

import com.yp.pay.base.exception.BusinessException;
import com.yp.pay.entity.dto.WxProfitShareQueryDTO;
import com.yp.pay.entity.dto.WxProfitShareReceiverAddDTO;
import com.yp.pay.entity.dto.WxProfitShareReturnDTO;
import com.yp.pay.entity.dto.WxSingleProfitShareDTO;
import com.yp.pay.entity.req.*;

public interface WxProfitShareService {

    /**
     * 单次分账（分账后剩余资金会自动解冻到商户账户，不需要调用完结分账接口）
     * @return WXSingleProfitShareDTO
     * @throws BusinessException
     */
    WxSingleProfitShareDTO singleProfitShare(WxProfitShareSingleReq wxProfitShareSingleReq) throws BusinessException;


    /**
     * 多次分账（因为是多次分账，所以不知道什么时候完成分账，所以业务认为分账完成后需要调用完结分账接口）
     * @return
     * @throws BusinessException
     */
    WxSingleProfitShareDTO multiProfitShare(WxProfitShareSingleReq wxProfitShareSingleReq) throws BusinessException;

    /**
     * 查询分账结果
     * @return
     * @throws BusinessException
     */
    WxProfitShareQueryDTO profitShareQuery(WxProfitShareQueryReq wxProfitShareQueryReq) throws BusinessException;

    /**
     * 添加分账接收方
     * @return
     * @throws BusinessException
     */
    WxProfitShareReceiverAddDTO profitShareAddReceiver(WxProfitShareReceiverAddReq wxProfitShareReceiverAddReq) throws BusinessException;

    /**
     * 删除分账接收方
     * @return
     * @throws BusinessException
     */
    WxProfitShareReceiverAddDTO profitShareRemoveReceiver(WxProfitShareReceiverSimpleReq wxProfitShareReceiverSimpleReq) throws BusinessException;

    /**
     * 完結分账
     * @return
     * @throws BusinessException
     */
    WxSingleProfitShareDTO profitShareFinish(WxProfitShareFinishReq wxProfitShareFinishReq) throws BusinessException;

    /**
     * 分账回退
     *
     * 在分账完成后，但是订单需要退款，这个时候就需要分账回退，然后在将商户的钱退款到用户账户
     * @return
     * @throws BusinessException
     */
    WxProfitShareReturnDTO profitShareReturn(WxProfitShareReturnReq wxProfitShareReturnReq) throws BusinessException;

    /**
     * 分账回退结果查询
     * @return
     * @throws BusinessException
     */
    WxProfitShareReturnDTO profitShareReturnQuery(WxProfitShareReturnQueryReq wxProfitShareReturnQueryReq) throws BusinessException;

}
