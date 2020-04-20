package com.yp.pay.wx.config;

import com.yp.pay.base.exception.BusinessException;
import com.yp.pay.common.enums.WXPayMethodType;
import com.yp.pay.common.util.StringUtil;
import com.yp.pay.wx.entity.req.*;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 基本工具类
 *
 * @author liuX
 * @date 20191212
 */
public class CommonUtil {


    /**
     * 将实体类转化成MAP对象
     *
     * @param object 需要转化的实体类
     * @return 转化后的Map对象
     */
    public static Map<String, String> getFiledInfo(Object object) throws Exception {

        Map<String, String> parameters = new HashMap<>();
        Field[] fields = object.getClass().getDeclaredFields();

        for (Field field : fields) {

            field.setAccessible(true);
            if (field.get(object) != null && field.get(object) != "null") {
                parameters.put(field.getName(), String.valueOf(field.get(object)));
            }
        }

        return parameters;
    }

    /**
     * 将请求实体类对象转化成微信需要的Map数据类型
     *
     * @param object 请求数据实体类对象
     * @param type   微信接口类型
     * @return 微信需要的MAP对象数据
     * @throws Exception
     */
    public static Map<String, String> getMapFromObject(Object object, Integer type) throws BusinessException {

        Map<String, String> map = new HashMap<>();

        // 付款码支付 || 扫码支付
        if (WXPayMethodType.microPay.getCode().equals(type) || WXPayMethodType.scanPay.getCode().equals(type)) {

            if (object instanceof WXMicroPayReq) {

                WXMicroPayReq microPayReq = (WXMicroPayReq) object;

                String deviceInfo = microPayReq.getDeviceInfo();
                if (StringUtils.isNotEmpty(deviceInfo)) {
                    map.put("device_info", deviceInfo);
                }

                String authCode = microPayReq.getAuthCode();
                map.put("auth_code", authCode);

                String subject = microPayReq.getSubject();
                map.put("body", subject);

                String detail = microPayReq.getDetail();
                if (StringUtils.isNotEmpty(detail)) {
                    map.put("detail", detail);
                }

                String attach = microPayReq.getAttach();
                if (StringUtils.isNotEmpty(attach)) {
                    // 将商户号信息添加到附加信息上，用户微信异步通知时，获取该商户的秘钥进行验签使用
                    map.put("attach", microPayReq.getMerchantNo()+"::"+attach);
                }

                String orderNo = microPayReq.getOrderNo();
                map.put("out_trade_no", orderNo);

                String clientIp = microPayReq.getClientIp();
                map.put("spbill_create_ip", clientIp);

                BigDecimal amount = microPayReq.getAmount();
                map.put("total_fee", StringUtil.formatYuanToFen(amount.toString()));

            } else if(object instanceof WXUnifiedPayReq){

                WXUnifiedPayReq wxUnifiedPayReq = (WXUnifiedPayReq) object;

                String deviceInfo = wxUnifiedPayReq.getDeviceInfo();
                if (StringUtils.isNotEmpty(deviceInfo)) {
                    map.put("device_info", deviceInfo);
                }

                String subject = wxUnifiedPayReq.getSubject();
                map.put("body", subject);

                String detail = wxUnifiedPayReq.getDetail();
                if (StringUtils.isNotEmpty(detail)) {
                    map.put("detail", detail);
                }

                String attach = wxUnifiedPayReq.getAttach();
                if (StringUtils.isNotEmpty(attach)) {
                    // 将商户号信息添加到附加信息上，用户微信异步通知时，获取该商户的秘钥进行验签使用
                    map.put("attach", wxUnifiedPayReq.getMerchantNo()+"::"+attach);
                }

                String orderNo = wxUnifiedPayReq.getOrderNo();
                map.put("out_trade_no", orderNo);

                BigDecimal amount = wxUnifiedPayReq.getAmount();
                map.put("total_fee", StringUtil.formatYuanToFen(amount.toString()));

                String clientIp = wxUnifiedPayReq.getClientIp();
                map.put("spbill_create_ip", clientIp);

                String tradeType = wxUnifiedPayReq.getTradeType();
                map.put("trade_type", tradeType);

                String openId = wxUnifiedPayReq.getOpenId();
                if (StringUtils.isNotEmpty(openId)) {
                    // trade_type=JSAPI时（即JSAPI支付），此参数必传，此参数为微信用户在商户对应appid下的唯一标识。
                    map.put("openid", openId);
                }

            } else {

                throw new BusinessException("请求实体类转化异常");
            }

            // 订单查询和撤销订单
        } else if (WXPayMethodType.orderQuery.getCode().equals(type) ||
                WXPayMethodType.reverse.getCode().equals(type)) {

            // 订单查询和撤销订单公用一个对象
            if (object instanceof WXOrderQueryOrReverseReq) {

                WXOrderQueryOrReverseReq orderQueryOrReverseReq = (WXOrderQueryOrReverseReq) object;
                String channelOrderNo = orderQueryOrReverseReq.getChannelOrderNo();
                if (StringUtils.isNotEmpty(channelOrderNo)) {
                    map.put("transaction_id", channelOrderNo);
                }

                String orderNo = orderQueryOrReverseReq.getOrderNo();
                if (StringUtils.isNotEmpty(orderNo)) {
                    map.put("out_trade_no", orderNo);
                }

                if (StringUtils.isBlank(orderNo) && StringUtils.isBlank(channelOrderNo)) {
                    throw new BusinessException("微信订单号channelOrderNo和商户订单号orderNo不能同时为空");
                }

            } else {
                throw new BusinessException("请求实体类转化异常");
            }
            // 统一下单中的关闭订单
        } else if (WXPayMethodType.closeOrder.getCode().equals(type)) {

            if (object instanceof WXCloseOrderReq) {

                WXCloseOrderReq wxCloseOrderReq = (WXCloseOrderReq) object;

                String orderNo = wxCloseOrderReq.getOrderNo();
                if (StringUtils.isNotEmpty(orderNo)) {
                    map.put("out_trade_no", orderNo);
                }

            } else {
                throw new BusinessException("请求实体类转化异常");
            }

            // 退款
        } else if (WXPayMethodType.refund.getCode().equals(type)) {

            if (object instanceof WXRefundReq) {

                WXRefundReq refundReq = (WXRefundReq) object;

                String OriginalChannelOrderNo = refundReq.getOriginalChannelOrderNo();
                if (StringUtils.isNotEmpty(OriginalChannelOrderNo)) {
                    map.put("transaction_id", OriginalChannelOrderNo);
                }

                String OriginalOrderNo = refundReq.getOriginalOrderNo();
                if (StringUtils.isNotEmpty(OriginalOrderNo)) {
                    map.put("out_trade_no", OriginalOrderNo);
                }

                if (StringUtils.isBlank(OriginalOrderNo) && StringUtils.isBlank(OriginalChannelOrderNo)) {
                    throw new BusinessException("微信订单号OriginalChannelOrderNo和商户订单号OriginalOrderNo不能同时为空");
                }

                String refundOrderNo = refundReq.getRefundOrderNo();
                map.put("out_refund_no", refundOrderNo);

                BigDecimal amount = refundReq.getAmount();
                map.put("total_fee", StringUtil.formatYuanToFen(amount.toString()));

                BigDecimal refundAmount = refundReq.getRefundAmount();
                map.put("refund_fee", StringUtil.formatYuanToFen(refundAmount.toString()));

            } else {
                throw new BusinessException("请求实体类转化异常");
            }

            // 退款查询
        } else if (WXPayMethodType.refundQuery.getCode().equals(type)) {

            if (object instanceof WXRefundQueryReq) {

                WXRefundQueryReq refundQueryReq = (WXRefundQueryReq) object;

                String orderNo = refundQueryReq.getOriginalOrderNo();
                if (StringUtils.isNotEmpty(orderNo)) {
                    map.put("out_trade_no", orderNo);
                }

                String refundOrderNo = refundQueryReq.getRefundOrderNo();
                if (StringUtils.isNotEmpty(refundOrderNo)) {
                    map.put("out_refund_no", refundOrderNo);
                }

                String channelOrderNo = refundQueryReq.getOriginalChannelOrderNo();
                if (StringUtils.isNotEmpty(channelOrderNo)) {
                    map.put("transaction_id", channelOrderNo);
                }

                String channelRefundOrderNo = refundQueryReq.getChannelRefundOrderNo();
                if (StringUtils.isNotEmpty(channelRefundOrderNo)) {
                    map.put("refund_id", channelRefundOrderNo);
                }

                if(StringUtils.isBlank(orderNo) && StringUtils.isBlank(refundOrderNo)
                        && StringUtils.isBlank(channelOrderNo) && StringUtils.isBlank(channelRefundOrderNo)){
                    throw new BusinessException("商户订单号,微信订单号,商户退款单号,微信退款单号 不能全部为空");
                }

            } else {
                throw new BusinessException("请求实体类转化异常");
            }

            // 下载对账单
        } else if (WXPayMethodType.downloadBill.getCode().equals(type)) {

            if (object instanceof WXDownloadBillReq) {

                WXDownloadBillReq wxDownloadBillReq = (WXDownloadBillReq) object;

                String billDate = wxDownloadBillReq.getBillDate();
                map.put("bill_date", billDate);

            } else {

                throw new BusinessException("请求实体类转化异常");
            }

        } else {

            throw new BusinessException("请求实体类转化异常，没有该接口的请求类型。");
        }

        return map;
    }

}
