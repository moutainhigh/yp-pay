package com.yp.pay.wx.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.yp.pay.base.exception.BusinessException;
import com.yp.pay.common.enums.PayRefundStatus;
import com.yp.pay.common.enums.RefundStatus;
import com.yp.pay.common.util.AESUtil;
import com.yp.pay.common.util.MD5Util;
import com.yp.pay.common.util.StringUtil;
import com.yp.pay.common.util.XMLParserUtil;
import com.yp.pay.entity.dto.CallBackInfoDTO;
import com.yp.pay.entity.dto.CallBackInfoDetailDTO;
import com.yp.pay.entity.dto.RefundCallBackInfoDTO;
import com.yp.pay.entity.dto.RefundCallBackInfoDetailDTO;
import com.yp.pay.entity.entity.TradePaymentRecordDO;
import com.yp.pay.entity.entity.TradeRefundRecordDO;
import com.yp.pay.wx.config.JWellWXPayConfig;
import com.yp.pay.wx.handler.WxPayHandler;
import com.yp.pay.wx.mapper.TradePaymentRecordMapper;
import com.yp.pay.wx.mapper.TradeRefundRecordMapper;
import com.yp.pay.wx.service.WxPayCallBackService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WxPayCallBackServiceImpl implements WxPayCallBackService {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TradePaymentRecordMapper tradePaymentRecordMapper;

    @Autowired
    private TradeRefundRecordMapper tradeRefundRecordMapper;

    private static final String SUCCESS = "SUCCESS";

    private static final String CHARSET = "UTF-8";

    private static final String RETURN_FORMATTER = "yyyyMMddhhmmss";

    private final static String PAY_TYPE = "WX_PAY";

    @Override
    public void scanPayNotify(HttpServletRequest request, HttpServletResponse response) throws BusinessException {

        ByteArrayOutputStream inBuffer = null;
        InputStream input = null;
        String requestXmlStr = null;
        try {

            inBuffer = new ByteArrayOutputStream();
            input = request.getInputStream();

            byte[] tmp = new byte[1024];

            int len = 0;
            while ((len = input.read(tmp)) > 0) {
                inBuffer.write(tmp, 0, len);
            }

            if (inBuffer.size() > 0) {

                byte[] requestData = inBuffer.toByteArray();
                requestXmlStr = new String(requestData, CHARSET);

                logger.info("微信异步通知数据 {}", requestXmlStr);
                if (StringUtils.isBlank(requestXmlStr)) {

                    new BusinessException("微信异步通知数据为空。");
                }
            }
        } catch (Exception e) {
            throw new BusinessException("接收微信异步通知信息异常");
        } finally {
            try {
                if (null != inBuffer) {
                    inBuffer.close();
                }
                if (null != input) {
                    input.close();
                }
            } catch (IOException e) {
            }
        }

        CallBackInfoDTO data = dealWXPayCallBackData(requestXmlStr);

        JSONObject postData = new JSONObject();

        boolean success = false;
        String msg;

        String url;

        url = data.getUrl();
        String returnCode;
        String returnMsg;
        Integer code;
        if (data.isDealSuccess()) {
            code = 200;
            returnCode = SUCCESS;
            returnMsg = "OK";
            success = true;
            msg = SUCCESS;

            CallBackInfoDetailDTO callBackInfoDetailDTO = data.getCallBackInfoDetailDTO();
            JSONObject detail = new JSONObject();
            detail.put("mechId", callBackInfoDetailDTO.getMerchantNo());
            detail.put("totalFee", callBackInfoDetailDTO.getTotalFee());
            detail.put("transactionId", callBackInfoDetailDTO.getTransactionId());
            detail.put("outTradeNo", callBackInfoDetailDTO.getOrderNo());
            detail.put("attach", callBackInfoDetailDTO.getAttach());
            detail.put("timeEnd", callBackInfoDetailDTO.getTimeEnd());
            detail.put("payType", callBackInfoDetailDTO.getPayType());
            postData.put("data", detail);
        } else {
            String message = data.getMessage();
            code = 600;
            returnCode = "FAIL";
            returnMsg = message;
            msg = message;
            logger.error(returnMsg);
        }

        // 异步通知数码仓

        postData.put("code", code);
        postData.put("msg", msg);
        postData.put("success", success);


        // TODO 暂时注释掉 无通知地址
//        RestTemplate restTemplate = new RestTemplate();
//        try {
//            logger.info("准备发送通知信息，目标通知地址：" + url + "，通知数据：" + postData.toJSONString());
//            ResponseEntity<JSONObject> responseEntity = restTemplate.postForEntity(url, postData, JSONObject.class);
//            logger.info("异步通知完成，状态码：" + responseEntity.getStatusCode() + "，异步通知数据：" + responseEntity.getBody());
//        } catch (Exception e) {
//            logger.error("异步通知地址：" + url + "，没有返回响应信息。");
//            e.printStackTrace();
//        }

        // 将处理结果通知银行
        sendSuccessNotify(response, returnCode, returnMsg);

    }

    @Override
    public void refundNotify(HttpServletRequest request, HttpServletResponse response) throws BusinessException {

        ByteArrayOutputStream inBuffer = null;
        InputStream input = null;
        String requestXmlStr = null;
        try {

            inBuffer = new ByteArrayOutputStream();
            input = request.getInputStream();

            byte[] tmp = new byte[1024];

            int len = 0;
            while ((len = input.read(tmp)) > 0) {
                inBuffer.write(tmp, 0, len);
            }

            if (inBuffer.size() > 0) {

                byte[] requestData = inBuffer.toByteArray();
                requestXmlStr = new String(requestData, CHARSET);

                logger.info("微信退款异步通知数据 {}", requestXmlStr);
                if (StringUtils.isBlank(requestXmlStr)) {

                    new BusinessException("微信退款异步通知数据为空。");
                }
            }
        } catch (Exception e) {
            throw new BusinessException("接收微信异步通知信息异常");
        } finally {
            try {
                if (null != inBuffer) {
                    inBuffer.close();
                }
                if (null != input) {
                    input.close();
                }
            } catch (IOException e) {
            }
        }

        RefundCallBackInfoDTO data = null;
        try {
            data = dealWXRefundBackData(URLEncoder.encode(requestXmlStr, CHARSET));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        JSONObject postData = new JSONObject();

        boolean success = false;
        String msg;

        String url;
        url = data.getUrl();
        String returnCode;
        String returnMsg;
        Integer code;
        if (data.isDealSuccess()) {
            returnCode = SUCCESS;
            returnMsg = "OK";
            code = 200;
            success = true;
            msg = SUCCESS;

            RefundCallBackInfoDetailDTO refundCallBackInfoDetailDTO = data.getRefundCallBackInfoDetailDTO();
            JSONObject detail = new JSONObject();
            // 下面为返回调用方数据 TODO
            detail.put("mechId", refundCallBackInfoDetailDTO.getMerchantNo());
            detail.put("totalFee", refundCallBackInfoDetailDTO.getTotalFee());
            detail.put("refundFee", refundCallBackInfoDetailDTO.getRefundFee());
            detail.put("settlementRefundFee", refundCallBackInfoDetailDTO.getSettlementRefundFee());
            detail.put("refundStatus", refundCallBackInfoDetailDTO.getRefundStatus());
            detail.put("successTime", refundCallBackInfoDetailDTO.getSuccessTime());
            detail.put("payType", refundCallBackInfoDetailDTO.getPayType());
            postData.put("data", detail);
        } else {
            String message = data.getMessage();
            code = 600;
            returnCode = "FAIL";
            returnMsg = message;
            msg = message;
            logger.error(returnMsg);
        }

        // 异步通知数码仓

        postData.put("code", code);
        postData.put("msg", msg);
        postData.put("success", success);

        // TODO 暂时注释掉 无通知地址
//        RestTemplate restTemplate = new RestTemplate();
//        try {
//            logger.info("准备发送通知信息，目标通知地址：" + url + "，通知数据：" + postData.toJSONString());
//            ResponseEntity<JSONObject> responseEntity = restTemplate.postForEntity(url, postData, JSONObject.class);
//            logger.info("异步通知完成，状态码：" + responseEntity.getStatusCode() + "，异步通知数据：" + responseEntity.getBody());
//        } catch (Exception e) {
//            logger.error("异步通知地址：" + url + "，没有返回响应信息。");
//            e.printStackTrace();
//        }

        // 将处理结果通知银行
        sendSuccessNotify(response, returnCode, returnMsg);

    }

    /**
     * 响应通知
     */
    public void sendSuccessNotify(HttpServletResponse response, String returnCode, String returnMsg) throws BusinessException {

        Writer writer;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
            // 通知微信已经收到消息，不要再给我发消息了，否则微信会8连击调用本接口
            String noticeStr = setXML(returnCode, returnMsg);
            logger.info("通知微信处理结果，通知数据：" + noticeStr);
            writer.write(noticeStr);
            writer.flush();

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * 处理完成微信异步通知，返回微信处理结果
     *
     * @param return_code
     * @param return_msg
     * @return
     */
    public static String setXML(String return_code, String return_msg) {
        return "<xml><return_code><![CDATA[" + return_code + "]]></return_code><return_msg><![CDATA[" + return_msg + "]]></return_msg></xml>";
    }

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

                        JWellWXPayConfig jWellWXPayConfig = WxPayHandler.merchantInfoMap.get(merchantNo);

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
                                updateData.setPaySuccessTime(date);
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

    public RefundCallBackInfoDTO dealWXRefundBackData(String xmlData) {

        RefundCallBackInfoDTO refundCallBackInfoDTO = new RefundCallBackInfoDTO();
        RefundCallBackInfoDetailDTO refundCallBackInfoDetailDTO = new RefundCallBackInfoDetailDTO();
        refundCallBackInfoDetailDTO.setPayType(PAY_TYPE);

        try {

            Map<String, String> resultMap = new HashMap<>(16);
            logger.info("微信异步通知返回数据 {}", xmlData);
            if (StringUtils.isNotBlank(xmlData)) {

                XMLParserUtil.parseByName(xmlData, resultMap);

                if (!resultMap.isEmpty()) {

                    TradePaymentRecordDO recordData = new TradePaymentRecordDO();
                    TradeRefundRecordDO updateRefund = new TradeRefundRecordDO();

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
                        JWellWXPayConfig jWellWXPayConfig = WxPayHandler.WXMerIdInfoMap.get(mchId);
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
                        updateRefund.setMerchantOrderNo(outTradeNo);
                        recordData.setMerchantOrderNo(outTradeNo);
                        // 查询出原支付订单
                        TradePaymentRecordDO findPaymentInfo = tradePaymentRecordMapper.selectOne(recordData);

                        refundCallBackInfoDetailDTO.setOriginalOrderNo(outTradeNo);

                        // 微信订单号（原微信支付订单号）
                        String transactionId = resultMap.get("transaction_id");
                        refundCallBackInfoDetailDTO.setOriginalTransactionId(transactionId);

                        // 商户退款单号
                        String outRefundNo = resultMap.get("out_refund_no");
                        updateRefund.setRefundOrderNo(outRefundNo);
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
                        jWellWXPayConfig = WxPayHandler.merchantInfoMap.get(merchantNo);

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
                        refundCallBackInfoDetailDTO.setSettlementRefundFee(StringUtil.formatFenToYuan(settlementRefundFee));

                        // 退款状态 SUCCESS-退款成功 CHANGE-退款异常 REFUNDCLOSE—退款关闭
                        String refundStatus = resultMap.get("refund_status");
                        refundCallBackInfoDetailDTO.setRefundStatus(refundStatus);
                        if (SUCCESS.equals(refundStatus)) {
                            updateRefund.setStatus(RefundStatus.REFUND_SUCCESS.getCode());

                            // 退款成功时间
                            String successTime = resultMap.get("success_time");
                            if (StringUtils.isNotBlank(successTime)) {
                                SimpleDateFormat sdf = new SimpleDateFormat(RETURN_FORMATTER);
                                Date date = sdf.parse(successTime);
                                // 退款完成时间
                                updateRefund.setRefundSuccessTime(date);
                                // 退款成功金额
                                updateRefund.setSuccessRefundAmount(Integer.parseInt(settlementRefundFee));
                                refundCallBackInfoDetailDTO.setSuccessTime(successTime);
                            }

                            findPaymentInfo.setRefundStatus(PayRefundStatus.REFUND_PART.getCode());
                            if (totalFee.equals(refundFee)) {
                                findPaymentInfo.setRefundStatus(PayRefundStatus.REFUND_ALL.getCode());
                            }

                            int i = tradePaymentRecordMapper.updateByPrimaryKeySelective(findPaymentInfo);
                            if (i < 1) {
                                logger.error("退款完成后，更新订单数据表中订单退款相关字段失败，请手动处理");
                            }

                        } else {
                            updateRefund.setStatus(RefundStatus.REFUND_FAIL.getCode());
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
                            tradePaymentRecordMapper.updateRecodeByInput(recordData);
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
