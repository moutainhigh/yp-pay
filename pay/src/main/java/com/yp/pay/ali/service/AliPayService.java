package com.yp.pay.ali.service;


import com.yp.pay.base.exception.BusinessException;
import com.yp.pay.entity.aliandwx.dto.MerchantInfoDTO;
import com.yp.pay.entity.aliandwx.dto.TradePaymentRecordDTO;
import com.yp.pay.entity.aliandwx.req.*;

/**
 * @author: lijiang
 * @date: 2019.12.11 15:56
 * @description: AliPayService
 */
public interface AliPayService {

    String scanningPay(AliF2FPayReq req) throws BusinessException;

    void queryScanningPayResultJob();

    TradePaymentRecordDTO getScanningPayPlatResult(AliOrderQueryReq aliOrderQueryReq) throws BusinessException;

    String webPay(AliWebPayReq req) throws BusinessException;

    TradePaymentRecordDTO webPayQuery(AliWebQueryReq req) throws BusinessException;

    MerchantInfoDTO queryMerchantByPayCode(MerchantQueryReq req);

    Boolean aliCancelPay(AliCancelPayReq req) throws BusinessException;
}
