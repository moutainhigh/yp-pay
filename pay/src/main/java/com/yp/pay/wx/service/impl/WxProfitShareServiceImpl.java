package com.yp.pay.wx.service.impl;

import com.yp.pay.base.exception.BusinessException;
import com.yp.pay.entity.aliandwx.dto.WxProfitShareQueryDTO;
import com.yp.pay.entity.aliandwx.dto.WxProfitShareReceiverAddDTO;
import com.yp.pay.entity.aliandwx.dto.WxProfitShareReturnDTO;
import com.yp.pay.entity.aliandwx.dto.WxSingleProfitShareDTO;
import com.yp.pay.entity.aliandwx.req.*;
import com.yp.pay.wx.service.WxProfitShareService;
import org.springframework.stereotype.Service;

@Service
public class WxProfitShareServiceImpl implements WxProfitShareService {

    @Override
    public WxSingleProfitShareDTO singleProfitShare(WxProfitShareSingleReq wxProfitShareSingleReq) throws BusinessException {
        return null;
    }

    @Override
    public WxSingleProfitShareDTO multiProfitShare(WxProfitShareSingleReq wxProfitShareSingleReq) throws BusinessException {
        return null;
    }

    @Override
    public WxProfitShareQueryDTO profitShareQuery(WxProfitShareQueryReq wxProfitShareQueryReq) throws BusinessException {
        return null;
    }

    @Override
    public WxProfitShareReceiverAddDTO profitShareAddReceiver(WxProfitShareReceiverAddReq wxProfitShareReceiverAddReq) throws BusinessException {
        return null;
    }

    @Override
    public WxProfitShareReceiverAddDTO profitShareRemoveReceiver(WxProfitShareReceiverSimpleReq wxProfitShareReceiverSimpleReq) throws BusinessException {
        return null;
    }

    @Override
    public WxSingleProfitShareDTO profitShareFinish(WxProfitShareFinishReq wxProfitShareFinishReq) throws BusinessException {
        return null;
    }

    @Override
    public WxProfitShareReturnDTO profitShareReturn(WxProfitShareReturnReq wxProfitShareReturnReq) throws BusinessException {
        return null;
    }

    @Override
    public WxProfitShareReturnDTO profitShareReturnQuery(WxProfitShareReturnQueryReq wxProfitShareReturnQueryReq) throws BusinessException {
        return null;
    }
}
