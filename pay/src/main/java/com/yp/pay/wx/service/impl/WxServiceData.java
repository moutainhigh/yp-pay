package com.yp.pay.wx.service.impl;

import com.yp.pay.base.exception.BusinessException;
import com.yp.pay.common.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @description: 微信公共变量和方法处理类
 *
 * @author: liuX
 * @time: 2020/7/5 8:59
 */
public class WxServiceData {

    static Logger logger = LoggerFactory.getLogger(WxServiceData.class);

    /**
     * 1、通信情况标识 SUCCESS：通信成功
     * 2、业务结果 SUCCESS：通信成功
     */
    protected static final String SUCCESS = "SUCCESS";

    /**
     * 1、通信情况标识 FAIL：通信失败
     * 2、业务结果 FAIL：通信失败
     */
    protected final static String FAIL = "FAIL";

    protected static final String CHARSET = "UTF-8";

    /**
     * 微信报文信息中支付完成时间格式
     */
    protected static final String RETURN_FORMATTER = "yyyyMMddhhmmss";

    /**
     * 微信渠道支付标志
     */
    protected final static String PAY_TYPE = "WX_PAY";

    /**
     * 查询（下载）对账单的日期格式
     */
    protected static final String INPUT_FORMATTER = "yyyyMMdd";

    /**
     * 平台支付订单号前缀
     */
    protected static final String PREFIX_PAY = "PAY";

    /**
     * 平台订单号组成中间段
     */
    protected static final String PLAT_ORDER_PART = "yyyyMMddhhmmss";

    /**
     * 平台退款订单号前缀
     */
    protected static final String PREFIX_RETURN = "RETURN";

    /**
     * 微信对账单中日期格式
     */
    protected static final String CHANNEL_BILL_FORMATTER = "yyyy-MM-dd HH:mm:ss";

    /**
     * 微信支付通知地址尾缀
     */
    protected final static String PAY_NOTICE_URL = "/scanPayNotify";

    /**
     * 微信退款通知地址尾缀
     */
    protected final static String REFUND_NOTICE_URL = "/refundNotify";

    /**
     * 微信JSAPI支付[公众号支付]标志 JSAPI
     */
    protected static final String WX_JS_API = "JSAPI";

    /**
     * 微信付款码支付标志 PAYMENT_CODE
     */
    protected static final String PAYMENT_CODE = "PAYMENT_CODE";

    /**
     * 分账支付标志 Y：分账支付
     */
    protected static final String PROFIT_SHARE = "Y";

    /**
     * 平台分账支付订单号前缀
     */
    protected final static String PREFIX_SHARE = "SHARE";

    protected static final String PREFIX_SHA_PAY = "SHAPAY";
    protected static final String PREFIX_SHA_RETURN = "SHARET";

    /**
     * 请求单次分账地址
     */
    protected final static String SINGLE_PROFIT_URL = "/secapi/pay/profitsharing";

    /**
     * 请求多次分账地址
     */
    protected final static String MULTI_PROFIT_URL = "/secapi/pay/multiprofitsharing";

    /**
     * 查询分账结果请求地址
     */
    protected final static String PROFIT_QUERY_URL = "/pay/profitsharingquery";

    /**
     * 添加分账接收方请求地址
     */
    protected final static String PROFIT_RECEIVER_ADD_URL = "/pay/profitsharingaddreceiver";

    /**
     * 删除分账接收方请求地址
     */
    protected final static String PROFIT_RECEIVER_REMOVE_URL = "/pay/profitsharingremovereceiver";

    /**
     * 完结分账请求地址
     */
    protected final static String PROFIT_FINISH_URL = "/secapi/pay/profitsharingfinish";

    /**
     * 分账回退请求地址
     */
    protected final static String PRIFIT_RETURN_URL = "/secapi/pay/profitsharingreturn";

    /**
     * 回退结果查询请求地址
     */
    protected final static String PRIFIT_RETURN_QUERY_URL = "/pay/profitsharingreturnquery";

    /**
     * 请求单次分账标志
     */
    protected final static String SINGLE = "SINGLE";

    /**
     * 请求多次分账标志
     */
    protected final static String MULTI = "MULTI";

    protected static void ipValidator(String clientIp) throws BusinessException{
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

}
