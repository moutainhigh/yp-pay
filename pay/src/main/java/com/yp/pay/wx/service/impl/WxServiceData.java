package com.yp.pay.wx.service.impl;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.yp.pay.base.exception.BusinessException;
import com.yp.pay.common.enums.WxPayMethodType;
import com.yp.pay.common.util.StringUtil;
import com.yp.pay.wx.config.WxMerchantInfo;
import com.yp.pay.wx.handler.WxPayHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @description: 微信公共变量和方法处理类
 *
 * @author: liuX
 * @time: 2020/7/5 8:59
 */
public class WxServiceData {

    private static Logger logger = LoggerFactory.getLogger(WxServiceData.class);

    /**
     * 1、通信情况标识 SUCCESS：通信成功
     * 2、业务结果 SUCCESS：通信成功
     */
    static final String SUCCESS = "SUCCESS";

    /**
     * 1、通信情况标识 FAIL：通信失败
     * 2、业务结果 FAIL：通信失败
     */
    final static String FAIL = "FAIL";

    static final String CHARSET = "UTF-8";

    /**
     * 微信报文信息中支付完成时间格式
     */
    static final String RETURN_FORMATTER = "yyyyMMddhhmmss";

    /**
     * 微信渠道支付标志
     */
    final static String PAY_TYPE = "WX_PAY";

    /**
     * 查询（下载）对账单的日期格式
     */
    static final String INPUT_FORMATTER = "yyyyMMdd";

    /**
     * 平台支付订单号前缀
     */
    static final String PREFIX_PAY = "PAY";

    /**
     * 平台订单号组成中间段
     */
    static final String PLAT_ORDER_PART = "yyyyMMddhhmmss";

    /**
     * 平台退款订单号前缀
     */
    static final String PREFIX_RETURN = "RETURN";

    /**
     * 微信对账单中日期格式
     */
    static final String CHANNEL_BILL_FORMATTER = "yyyy-MM-dd HH:mm:ss";

    /**
     * 微信支付通知地址尾缀
     */
    final static String PAY_NOTICE_URL = "/scanPayNotify";

    /**
     * 微信退款通知地址尾缀
     */
    final static String REFUND_NOTICE_URL = "/refundNotify";

    /**
     * 微信JSAPI支付[公众号支付]标志 JSAPI
     */
    static final String WX_JS_API = "JSAPI";

    /**
     * 微信付款码支付标志 PAYMENT_CODE
     */
    static final String PAYMENT_CODE = "PAYMENT_CODE";

    /**
     * 分账支付标志 Y：分账支付
     */
    static final String PROFIT_SHARE = "Y";

    /**
     * 平台分账支付订单号前缀
     */
    final static String PREFIX_SHARE = "SHARE";

    static final String PREFIX_SHA_PAY = "SHAPAY";
    static final String PREFIX_SHA_RETURN = "SHARET";

    /**
     * 请求单次分账地址
     */
    final static String SINGLE_PROFIT_URL = "/secapi/pay/profitsharing";

    /**
     * 请求多次分账地址
     */
    final static String MULTI_PROFIT_URL = "/secapi/pay/multiprofitsharing";

    /**
     * 查询分账结果请求地址
     */
    final static String PROFIT_QUERY_URL = "/pay/profitsharingquery";

    /**
     * 添加分账接收方请求地址
     */
    final static String PROFIT_RECEIVER_ADD_URL = "/pay/profitsharingaddreceiver";

    /**
     * 删除分账接收方请求地址
     */
    final static String PROFIT_RECEIVER_REMOVE_URL = "/pay/profitsharingremovereceiver";

    /**
     * 完结分账请求地址
     */
    final static String PROFIT_FINISH_URL = "/secapi/pay/profitsharingfinish";

    /**
     * 分账回退请求地址
     */
    final static String PRIFIT_RETURN_URL = "/secapi/pay/profitsharingreturn";

    /**
     * 回退结果查询请求地址
     */
    final static String PRIFIT_RETURN_QUERY_URL = "/pay/profitsharingreturnquery";

    /**
     * 请求单次分账标志
     */
    final static String SINGLE = "SINGLE";

    /**
     * 请求多次分账标志
     */
    final static String MULTI = "MULTI";

    /**
     * IP校验方法
     *
     * @author liuX
     * @time 2020/7/8 15:33
     * @param clientIp IP地址
     * @return
     *
     */
    static void ipValidator(String clientIp) throws BusinessException{
        if(StringUtils.isBlank(clientIp)){
            throw new BusinessException("请求数据中终端IP[clientIp]数据不能为空，请核对数据。");
        }
        if(!StringUtil.ip4Validator(clientIp)){
            logger.error("请求数据中终端IP[clientIp]数据格式不是TCP/IPv4的IP格式。源数据clientIp："+clientIp);
            if(!StringUtil.ip6Validator(clientIp)){
                logger.error("请求数据中终端IP[clientIp]数据格式不是TCP/IPv4的IP格式。源数据clientIp："+clientIp);
                throw new BusinessException("请求数据中终端IP[clientIp]数据格式有误，请核对数据。");
            }
        }
    }

    /**
     * 请求微信接口通用方法
     *
     * @author liuX
     * @time 2020/7/8 15:31
     * @param wxMerchantInfo 商户微信配置信息
     * @param reqData 请求数据
     * @param methodType 请求接口类型
     * @return 返回数据
     *
     */
    static Map<String, String> postAndReceiveData(WxMerchantInfo wxMerchantInfo,
                                                          Map<String, String> reqData, Integer methodType) throws BusinessException {
        WXPay wxPay;
        try {
            wxPay = new WXPay(wxMerchantInfo);
        } catch (Exception e) {
            throw new BusinessException("微信支付初始化异常。");
        }

        Map<String, String> response = null;
        WxPayMethodType payMethodType = WxPayMethodType.getByCode(methodType);
        if (payMethodType == null) {
            throw new BusinessException("您请求的接口类型不存在，请确认");
        }
        String reqTypeDesc = payMethodType.getMessage();

        Object payType = reqData.get("trade_type");

        try {

            String detail;
            if (payType != null) {
                detail = "]中的[" + payType + "]支付";
            } else {
                detail = "]。";
            }

            logger.info("请求微信接口数据：" + reqData.toString() + "，请求接口类型：[" + reqTypeDesc + detail);
            if (WxPayMethodType.microPay.getCode().equals(methodType)) {
                response = wxPay.microPay(reqData);

            } else if (WxPayMethodType.scanPay.getCode().equals(methodType)) {
                response = wxPay.unifiedOrder(reqData);

            } else if (WxPayMethodType.orderQuery.getCode().equals(methodType)) {
                response = wxPay.orderQuery(reqData);

            } else if (WxPayMethodType.closeOrder.getCode().equals(methodType)) {
                response = wxPay.closeOrder(reqData);

            } else if (WxPayMethodType.reverse.getCode().equals(methodType)) {
                response = wxPay.reverse(reqData);

            } else if (WxPayMethodType.refund.getCode().equals(methodType)) {
                response = wxPay.refund(reqData);

            } else if (WxPayMethodType.refundQuery.getCode().equals(methodType)) {
                response = wxPay.refundQuery(reqData);

            } else if (WxPayMethodType.downloadBill.getCode().equals(methodType)) {
                response = wxPay.downloadBill(reqData);

            } else if (WxPayMethodType.authCodeToOpenid.getCode().equals(methodType)) {
                response = wxPay.authCodeToOpenid(reqData);

            } else {
                logger.error("不存在你请求的交易类别！");
            }
        } catch (Exception e) {
            logger.error("调用微信支付API接口异常。");
        }
        if (response == null) {
            throw new BusinessException(reqTypeDesc + "的响应信息为空，请核实请求和响应数据");
        }
        logger.info(reqTypeDesc + "的返回信息：" + response);

        return response;
    }

    /**
     * 分账支付请求微信通用方法
     *
     * @author liuX
     * @time 2020/7/8 17:03
     * @param url 请求地址
     * @param withCert 是否需要证书
     * @param postData 请求数据
     * @param config 商户配置信息
     * @return
     *
     */
    Map<String, String> postAndReceiveData(String url, Boolean withCert, Map<String, String> postData, WxMerchantInfo config) throws BusinessException {

        WXPay wxPay;
        try {
            wxPay = new WXPay(config);
        } catch (Exception e) {
            throw new BusinessException("微信支付初始化异常。");
        }

        Map<String, String> fillMap;
        try {
            // 分账查询组装数据的时候请求参数没有APPID
            if (PROFIT_QUERY_URL.equals(url)) {
                fillMap = fillRequestData(postData, config);
            } else {
                fillMap = wxPay.fillRequestData(postData);
            }
            logger.info("提交微信请求接口数据：" + fillMap.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("提交微信请求前，生成签名数据异常。");
        }

        String response;
        try {
            if (withCert) {
                // 有证书
                response = wxPay.requestWithCert(url, fillMap, config.getHttpConnectTimeoutMs(), config.getHttpReadTimeoutMs());
            } else {
                // 没有证书
                response = wxPay.requestWithoutCert(url, fillMap, config.getHttpConnectTimeoutMs(), config.getHttpReadTimeoutMs());
            }

            if (StringUtils.isNotBlank(response)) {
                return  wxPay.processResponseXml(response);

            } else {
                throw new BusinessException("微信返回数据异常，返回信息为空");
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("提交微信请求时，发生异常。");
        }
    }

    /**
     * 分账查询的时候没有APPID
     *
     * @param reqData
     * @param config
     * @return
     * @throws Exception
     */
    private Map<String, String> fillRequestData(Map<String, String> reqData, WxMerchantInfo config) throws Exception {
        reqData.put("mch_id", config.getMchID());
        reqData.put("nonce_str", WXPayUtil.generateNonceStr());
        reqData.put("sign_type", "HMAC-SHA256");

        reqData.put("sign", WXPayUtil.generateSignature(reqData, config.getKey(), WXPayConstants.SignType.HMACSHA256));
        return reqData;
    }

    /**
     * 通过商户号获取商户微信渠道的配置信息（信息中含有证书）
     *
     * @param merchantNo
     * @return
     * @author liuX
     * @time 2020/7/5 10:59
     */
    static WxMerchantInfo getWxMerchantInfoWithCert(String merchantNo) throws BusinessException {
        WxMerchantInfo wxMerchantInfo = WxPayHandler.wxMerchantInfoMap.get(merchantNo);
        if (wxMerchantInfo == null) {
            throw new BusinessException("未获取到商户号为：" + merchantNo + "商户的相关配置信息，" +
                    "请联系相关工作人员进行商户数据确认或新增该商户" + merchantNo + "的配置信息。");
        }
        return wxMerchantInfo;
    }

}
