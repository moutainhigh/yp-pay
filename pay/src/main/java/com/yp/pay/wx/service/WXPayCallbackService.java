package com.yp.pay.wx.service;


import com.yp.pay.base.exception.BusinessException;
import com.yp.pay.entity.aliandwx.dto.CallBackInfoDTO;
import com.yp.pay.entity.aliandwx.dto.RefundCallBackInfoDTO;

public interface WXPayCallbackService {

    CallBackInfoDTO dealWXPayCallBackData(String xmlData) throws BusinessException;

    RefundCallBackInfoDTO dealWXRefundBackData(String xmlData) throws BusinessException;

}