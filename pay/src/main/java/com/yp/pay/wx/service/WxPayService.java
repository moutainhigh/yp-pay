package com.yp.pay.wx.service;

import com.yp.pay.base.exception.BusinessException;
import com.yp.pay.entity.dto.*;
import com.yp.pay.entity.req.*;

import java.util.Map;

/**
 * @description: 微信支付接口
 *
 * @author: liuX
 * @time: 2020/6/13 22:35
 */
public interface WxPayService {

    /**
     * 通过code获取openid
     *
     * @param code 用户code
     * @param merchantNo 商户号
     * @return
     * @throws BusinessException
     */
    String getOpenId(String code, String merchantNo) throws BusinessException;

    /**
     * 通过商户编号获取商户配置信息
     *
     * @param merchantNo：商户号
     * @param payWayCode：支付方式
     * @return MerchantInfoDTO
     * @throws BusinessException
     */
    MerchantInfoDTO merchantQuery(String merchantNo, String payWayCode) throws BusinessException;

    /**
     * 通过订单号和商户编号获取订单信息
     *
     * @param merchantNo 商户号
     * @param orderNo 订单号
     * @return
     * @throws BusinessException
     */
    TradePaymentRecordDTO getTradeOrderInfo(String merchantNo, String orderNo) throws BusinessException;

    /**
     * 作用：获取二维码链接
     * @param qrCodeInfoReq
     * @return API返回数据
     * @throws BusinessException
     */
    UnionPayCodeDTO getQrCodeInfo(QrCodeInfoReq qrCodeInfoReq) throws BusinessException;

    /**
     * 作用：付款码支付<br>
     * 场景：客户被扫
     * @param microPayReq 向wxpay post的请求数据
     * @return API返回数据
     * @throws BusinessException
     */
    MicroPayResultDTO microPay(WxMicroPayReq microPayReq) throws BusinessException;

    /**
     * 作用：统一下单<br>
     * 场景：公共号支付、扫码支付、APP支付
     * @param wxUnifiedPayReq 向wxpay post的请求数据
     * @return API返回数据
     * @throws BusinessException
     */
    ScanCodeDTO unifiedPay(WxUnifiedPayReq wxUnifiedPayReq) throws BusinessException;

    /**
     * 作用：APP支付<br>
     * 场景：公共号支付、扫码支付、APP支付
     * @param wxUnifiedPayReq 向wxpay post的请求数据
     * @return API返回数据
     * @throws BusinessException
     */
    WxAppPayDTO appPay(WxUnifiedPayReq wxUnifiedPayReq) throws BusinessException;

    /**
     * 作用：查询订单<br>
     * 场景：付款码支付、公共号支付、扫码支付、APP支付
     * @param orderQueryOrReverseReq 向wxpay post的请求数据
     * @return API返回数据
     * @throws BusinessException
     */
    TradePaymentRecordDTO orderQuery(WxOrderQueryOrReverseReq orderQueryOrReverseReq) throws BusinessException;

    /**
     * 作用：关闭订单<br>
     * 场景：统一支付
     * @param wxCloseOrderReq 向wxpay post的请求数据
     * @return API返回数据
     * @throws BusinessException
     */
    CloseOrderDTO closeOrder(WxCloseOrderReq wxCloseOrderReq) throws BusinessException;

    /**
     * 作用：撤销订单<br>
     * 场景：付款码支付
     * @param orderQueryOrReverseReq 向wxpay post的请求数据
     * @return API返回数据
     * @throws BusinessException
     */
    Map<String, String> reverse(WxOrderQueryOrReverseReq orderQueryOrReverseReq) throws BusinessException;

    /**
     * 作用：申请退款<br>
     * 场景：付款码支付、公共号支付、扫码支付、APP支付
     * @param refundReq 向wxpay post的请求数据
     * @return API返回数据
     * @throws BusinessException
     */
    ApplyRefundDTO refund(WxRefundReq refundReq) throws BusinessException;

    /**
     * 作用：退款查询<br>
     * 场景：付款码支付、公共号支付、扫码支付、APP支付
     * @param refundQueryReq 向wxpay post的请求数据
     * @return API返回数据
     * @throws BusinessException
     */
    RefundQueryDTO refundQuery(WxRefundQueryReq refundQueryReq) throws BusinessException;

    /**
     * 作用：对账单下载（成功时返回对账单数据，失败时返回XML格式数据）<br>
     * 场景：付款码支付、公共号支付、扫码支付、APP支付
     * @param wxDownloadBillReq 请求实体类数据
     * @return API返回数据
     * @throws BusinessException
     */
    BillDownloadDTO downloadBill(WxDownloadBillReq wxDownloadBillReq) throws BusinessException;

    /**
     * 作用：授权码查询OPENID接口<br>
     * 场景：付款码支付
     *
     * @param authCode 授权码
     * @param merchantNo 商户号
     * @return API返回数据
     * @throws BusinessException
     */
    Map<String, String> authCodeToOpenid(String authCode, String merchantNo) throws BusinessException;

}
