package com.yp.pay.wx.service.impl;

import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.yp.pay.base.exception.BusinessException;
import com.yp.pay.common.enums.AliAndWXPayStatus;
import com.yp.pay.common.util.AESUtil;
import com.yp.pay.common.util.MD5Util;
import com.yp.pay.common.util.StringUtil;
import com.yp.pay.common.util.XMLParserUtil;
import com.yp.pay.entity.aliandwx.dao.TradePaymentRecordDO;
import com.yp.pay.entity.aliandwx.dto.CallBackInfoDTO;
import com.yp.pay.entity.aliandwx.dto.CallBackInfoDetailDTO;
import com.yp.pay.entity.aliandwx.dto.RefundCallBackInfoDTO;
import com.yp.pay.entity.aliandwx.dto.RefundCallBackInfoDetailDTO;
import com.yp.pay.wx.config.JWellWXPayConfig;
import com.yp.pay.wx.handler.WXPayHandler;
import com.yp.pay.wx.mapper.TradePaymentRecordMapper;
import com.yp.pay.wx.service.WXPayCallbackService;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.security.MD5Encoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WXPayCallbackServiceImpl implements WXPayCallbackService {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TradePaymentRecordMapper tradePaymentRecordMapper;

    private final static String SUCCESS = "SUCCESS";

    private static final String RETURN_FORMATTER = "yyyyMMddhhmmss";

    private final static String PAY_TYPE = "WX_PAY";

    public CallBackInfoDTO dealWXPayCallBackData(String xmlData) {

        CallBackInfoDTO callBackInfoDTO = new CallBackInfoDTO();
        CallBackInfoDetailDTO callBackInfoDetailDTO = new CallBackInfoDetailDTO();
        callBackInfoDetailDTO.setPayType(PAY_TYPE);

        try {
            Map<String, String> resultMap = new HashMap<>();
            logger.info("微信异步通知返回数据 {}", xmlData);
            if (StringUtils.isNotBlank(xmlData)) {

                XMLParserUtil.parseByName(xmlData, resultMap);

                if (!resultMap.isEmpty()) {

                    String signStr = resultMap.remove("sign");

                    // attach 存放为商户号信息
                    String[] attach = resultMap.get("attach").split("::");

                    String merchantNo = attach[0];
                    callBackInfoDetailDTO.setMerchantNo(merchantNo);

                    if (attach.length == 2) {
                        callBackInfoDetailDTO.setAttach(attach[1]);
                    }

                    if (StringUtils.isBlank(merchantNo)) {
                        logger.error("无法获取商户号信息，故找不到对应的解密秘钥，无法验证数据的真伪。");
                        callBackInfoDTO.setDealSuccess(false);
                        callBackInfoDTO.setMessage("无法获取商户号信息，故找不到对应的解密秘钥，无法验证数据的真伪。");
                    } else {

                        JWellWXPayConfig jWellWXPayConfig = WXPayHandler.merchantInfoMap.get(merchantNo);

                        if (jWellWXPayConfig == null) {
                            logger.error("未获取到商户号为：" + merchantNo + "商户的相关配置信息，" +
                                    "请联系相关工作人员进行商户数据确认或新增该商户" + merchantNo + "的配置信息。");
                            callBackInfoDTO.setDealSuccess(false);
                            callBackInfoDTO.setMessage("未获取到商户号为：" + merchantNo + "商户的相关配置信息，" +
                                    "请联系相关工作人员进行商户数据确认或新增该商户" + merchantNo + "的配置信息。");
                        }
                        callBackInfoDTO.setUrl(jWellWXPayConfig.merchantPayInfoDO.getMerNotifyUrl());

                        String returnSign = WXPayUtil.generateSignature(resultMap, jWellWXPayConfig.getKey(), WXPayConstants.SignType.HMACSHA256);

                        if (signStr.equals(returnSign)) {

                            TradePaymentRecordDO updateData = new TradePaymentRecordDO();

                            // 商户订单号
                            String returnCode = resultMap.get("return_code");
                            String returnMsg = resultMap.get("return_msg");
                            if (StringUtils.isNotEmpty(returnCode) && SUCCESS.equals(returnCode)) {

                                // 商户订单号
                                String orderNo = resultMap.get("out_trade_no");
                                updateData.setMerchantOrderNo(orderNo);
                                callBackInfoDetailDTO.setOrderNo(orderNo);

                                // 订单金额 单位分 转化成元
                                String totalFee = resultMap.get("total_fee");
                                if (StringUtils.isNotBlank(totalFee)) {
                                    callBackInfoDetailDTO.setTotalFee(StringUtil.formatFenToYuan(totalFee));
                                }

                                // 支付金额 单位分 转化成元
                                String cashFee = resultMap.get("cash_fee");
                                if (StringUtils.isNotBlank(cashFee)) {
                                    callBackInfoDetailDTO.setCashFee(StringUtil.formatFenToYuan(cashFee));
                                }

                                // 业务结果 SUCCESS/FAIL
                                String resultCode = resultMap.get("result_code");
                                updateData.setErrCode(resultCode);
                                callBackInfoDetailDTO.setResultCode(resultCode);
                                if (SUCCESS.equals(resultCode)) {
                                    updateData.setStatus(2);
                                } else {
                                    updateData.setStatus(3);
                                }

                                // 如果 错误代码 和 错误描述 不为空则将 错误代码和错误描述更新到数据库中
                                String errCode = resultMap.get("err_code");
                                if (StringUtils.isNotBlank(errCode)) {
                                    updateData.setErrCode(errCode);
                                    callBackInfoDetailDTO.setErrCode(errCode);
                                }
                                String errCodeDes = resultMap.get("err_code_des");
                                if (StringUtils.isNotBlank(errCodeDes)) {
                                    updateData.setErrCodeDes(errCodeDes);
                                    callBackInfoDetailDTO.setErrCodeDes(errCodeDes);
                                }

                                String feeType = resultMap.get("fee_type");
                                callBackInfoDetailDTO.setFeeType("CNY");
                                if (StringUtils.isNotBlank(feeType)) {
                                    callBackInfoDetailDTO.setFeeType(feeType);
                                }

                                String openId = resultMap.get("openid");
                                callBackInfoDetailDTO.setOpenId(openId);


                                // 微信支付订单号
                                String transactionId = resultMap.get("transaction_id");
                                updateData.setChannelOrderNo(transactionId);
                                callBackInfoDetailDTO.setTransactionId(transactionId);

                                // 支付完成时间
                                String timeEnd = resultMap.get("time_end");
                                SimpleDateFormat sdf = new SimpleDateFormat(RETURN_FORMATTER);
                                Date date = sdf.parse(timeEnd);
                                updateData.setCompleteTime(date);
                                callBackInfoDetailDTO.setTimeEnd(timeEnd);

                                try {
                                    tradePaymentRecordMapper.updateRecodeByInput(updateData);
                                    callBackInfoDTO.setDealSuccess(true);
                                } catch (Exception e) {
                                    logger.error("微信异步通知后更新数据库数据状态异常。");
                                    callBackInfoDTO.setDealSuccess(false);
                                }

                            } else {
                                callBackInfoDTO.setDealSuccess(false);
                                callBackInfoDTO.setMessage("支付失败，银行异步通知返回错误代码:" + returnCode + ",错误描述：" + returnMsg);
                                logger.error("支付失败，银行异步通知返回错误代码:" + returnCode + ",错误描述：" + returnMsg);
                            }
                        } else {
                            logger.error("银行返回的异步通知验签失败");
                        }
                    }
                } else {
                    callBackInfoDTO.setDealSuccess(false);
                    callBackInfoDTO.setMessage("获取到微信支付异步通知Map数据为空，请核对微信异步通知报文");
                    logger.error("获取到微信支付异步通知Map数据为空，请核对微信异步通知报文");
                }
            } else {
                callBackInfoDTO.setDealSuccess(false);
                callBackInfoDTO.setMessage("微信异步通知请求数据为空，请核对数据。");
                logger.error("微信异步通知请求数据为空，请核对数据。");
            }
        } catch (Exception e) {
            callBackInfoDTO.setDealSuccess(false);
            callBackInfoDTO.setMessage("数据转化异常，请核对返回数据。");
            logger.error("数据转化异常，请核对返回数据。");
        }

        callBackInfoDTO.setCallBackInfoDetailDTO(callBackInfoDetailDTO);
        return callBackInfoDTO;
    }

    @Override
    public RefundCallBackInfoDTO dealWXRefundBackData(String xmlData) throws BusinessException {

        RefundCallBackInfoDTO refundCallBackInfoDTO = new RefundCallBackInfoDTO();
        RefundCallBackInfoDetailDTO refundCallBackInfoDetailDTO = new RefundCallBackInfoDetailDTO();
        refundCallBackInfoDetailDTO.setPayType(PAY_TYPE);

        try {

            Map<String, String> resultMap = new HashMap<>();
            logger.info("微信异步通知返回数据 {}", xmlData);
            if (StringUtils.isNotBlank(xmlData)) {

                XMLParserUtil.parseByName(xmlData, resultMap);

                if (!resultMap.isEmpty()) {

                    TradePaymentRecordDO updateData = new TradePaymentRecordDO();

                    // 返回状态码 SUCCESS/FAIL 此字段是通信标识，非交易标识，交易是否成功需要查看trade_state来判断
                    String returnCode = resultMap.get("return_code");
                    String returnMsg = resultMap.get("return_msg");
                    if (StringUtils.isNotEmpty(returnCode) && SUCCESS.equals(returnCode)) {

                        // 加密信息 加密信息请用商户秘钥进行解密
                        String reqInfo = resultMap.get("req_info");

                        /**
                         * 微信没有中间平台调用方的商户号，现在异步通知平台，平台需要异步通知调用方
                         * 处理过程如下：
                         *  1）通过微信的商户号获取秘钥信息
                         *  2）用秘钥进行解密，然后获取订单号
                         *  3）通过订单号获取调用平台的商户号
                         *  4）将商户号放入返回对象，并将异步通知地址放入数据进行返回
                         *
                         */
                        // 1) 退款的商户号 微信支付分配的商户号
                        String mchId = resultMap.get("mch_id");
                        JWellWXPayConfig jWellWXPayConfig = WXPayHandler.WXMerIdInfoMap.get(mchId);
                        String mchKey = jWellWXPayConfig.getKey();
                        String encryptKey = MD5Util.getMD5(mchKey);

                        // 2）用秘钥进行解密，然后获取订单号
                        // 对微信返回异步通知加密信息进行解密
                        String decryptData = AESUtil.decryptData(reqInfo, encryptKey);
                        logger.info("微信退款异步通知解密后的数据为：" + decryptData);

                        // 清楚之前数据
                        resultMap.clear();
                        // 将解密后的数据放入MAP中
                        XMLParserUtil.parseByName(decryptData, resultMap);

                        // 商户订单号(原商户支付订单号)
                        String outTradeNo = resultMap.get("out_trade_no");
                        updateData.setMerchantOrderNo(outTradeNo);
                        refundCallBackInfoDetailDTO.setOriginalOrderNo(outTradeNo);

                        // 微信订单号（原微信支付订单号）
                        String transactionId = resultMap.get("transaction_id");
                        refundCallBackInfoDetailDTO.setOriginalTransactionId(transactionId);

                        // 商户退款单号
                        String outRefundNo = resultMap.get("out_refund_no");
                        refundCallBackInfoDetailDTO.setRefundOrderNo(outRefundNo);

                        // 3）通过订单号获取调用平台的商户号
                        TradePaymentRecordDO tradePaymentRecordDO = new TradePaymentRecordDO();
                        tradePaymentRecordDO.setMerchantOrderNo(outTradeNo);
                        List<TradePaymentRecordDO> tradePaymentExist = tradePaymentRecordMapper.select(tradePaymentRecordDO);
                        if (tradePaymentExist == null || tradePaymentExist.size() == 0) {
                            throw new BusinessException("未查询到原支付订单号[" + outRefundNo + "]的支付订单数据。");
                        }
                        tradePaymentRecordDO = tradePaymentExist.get(0);
                        String merchantNo = tradePaymentRecordDO.getMerchantNo();

                        //  4）将商户号放入返回对象，并将异步通知地址放入数据进行返回
                        refundCallBackInfoDetailDTO.setMerchantNo(merchantNo);
                        jWellWXPayConfig = WXPayHandler.merchantInfoMap.get(merchantNo);

                        refundCallBackInfoDTO.setUrl(jWellWXPayConfig.merchantPayInfoDO.getMerRefundNotifyUrl());

                        // 微信退款单号
                        String refundId = resultMap.get("refund_id");
                        refundCallBackInfoDetailDTO.setChannelRefundOrderNo(refundId);

                        // 订单金额 单位分 转化成元
                        String totalFee = resultMap.get("total_fee");
                        refundCallBackInfoDetailDTO.setTotalFee(StringUtil.formatFenToYuan(totalFee));

                        // 申请退款金额 单位分 转化成元
                        String refundFee = resultMap.get("refund_fee");
                        refundCallBackInfoDetailDTO.setRefundFee(StringUtil.formatFenToYuan(refundFee));

                        // 退款金额 单位分 转化成元
                        String settlementRefundFee = resultMap.get("settlement_refund_fee");
                        String yuanFee = StringUtil.formatFenToYuan(settlementRefundFee);
                        updateData.setSuccessRefundAmount(new BigDecimal(yuanFee));
                        refundCallBackInfoDetailDTO.setSettlementRefundFee(yuanFee);

                        // 退款状态 SUCCESS-退款成功 CHANGE-退款异常 REFUNDCLOSE—退款关闭
                        String refundStatus = resultMap.get("refund_status");
                        refundCallBackInfoDetailDTO.setRefundStatus(refundStatus);
                        if (SUCCESS.equals(refundStatus)) {
                            updateData.setStatus(AliAndWXPayStatus.REFUND.getCode());
                        } else {
                            updateData.setStatus(AliAndWXPayStatus.REFUND_ERR.getCode());
                        }

                        // 退款成功时间
                        String successTime = resultMap.get("success_time");
                        if (StringUtils.isNotBlank(successTime)) {
                            SimpleDateFormat sdf = new SimpleDateFormat(RETURN_FORMATTER);
                            Date date = sdf.parse(successTime);
                            updateData.setRefundSuccessTime(date);
                            refundCallBackInfoDetailDTO.setSuccessTime(successTime);
                        }

                        /*
                         * 退款入账账户
                         * 1）退回银行卡 {银行名称}{卡类型}{卡尾号}
                         * 2）退回支付用户零钱: 支付用户零钱
                         * 3）退还商户: 商户基本账户 商户结算银行账户
                         * 4）退回支付用户零钱通: 支付用户零钱通
                         */
                        String refundRecvAccount = resultMap.get("refund_recv_accout");
                        refundCallBackInfoDetailDTO.setRefundRecvAccount(refundRecvAccount);

                        String refundAccount = resultMap.get("refund_account");
                        refundCallBackInfoDetailDTO.setRefundAccount(refundAccount);

                        String refundRequestSource = resultMap.get("refund_request_source");
                        refundCallBackInfoDetailDTO.setRefundRequestSource(refundRequestSource);

                        try {
                            tradePaymentRecordMapper.updateRecodeByInput(updateData);
                            refundCallBackInfoDTO.setDealSuccess(true);
                        } catch (Exception e) {
                            logger.error("微信异步通知后更新数据库数据状态异常。");
                            refundCallBackInfoDTO.setDealSuccess(false);
                        }

                    } else {
                        refundCallBackInfoDTO.setDealSuccess(false);
                        refundCallBackInfoDTO.setMessage("退款失败，银行异步通知返回错误代码:" + returnCode + ",错误描述：" + returnMsg);
                        logger.error("退款失败，银行异步通知返回错误代码:" + returnCode + ",错误描述：" + returnMsg);
                    }
                } else {
                    refundCallBackInfoDTO.setDealSuccess(false);
                    refundCallBackInfoDTO.setMessage("获取到微信支付异步通知Map数据为空，请核对微信异步通知报文");
                    logger.error("获取到微信支付异步通知Map数据为空，请核对微信异步通知报文");
                }
            } else {
                refundCallBackInfoDTO.setDealSuccess(false);
                refundCallBackInfoDTO.setMessage("微信异步通知请求数据为空，请核对数据。");
                logger.error("微信异步通知请求数据为空，请核对数据。");
            }
        } catch (Exception e) {
            e.printStackTrace();
            refundCallBackInfoDTO.setDealSuccess(false);
            refundCallBackInfoDTO.setMessage("数据转化异常，请核对返回数据。");
            logger.error("数据转化异常，请核对返回数据。");
        }

        refundCallBackInfoDTO.setRefundCallBackInfoDetailDTO(refundCallBackInfoDetailDTO);
        return refundCallBackInfoDTO;
    }

}
