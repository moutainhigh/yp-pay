package com.yp.pay.wx.service.impl;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.yp.pay.base.exception.BusinessException;
import com.yp.pay.common.enums.PayRefundStatus;
import com.yp.pay.common.enums.RefundStatus;
import com.yp.pay.common.enums.TradeStatus;
import com.yp.pay.common.enums.WxPayMethodType;
import com.yp.pay.common.util.EntityConverter;
import com.yp.pay.common.util.GlobalSysnoGenerator;
import com.yp.pay.common.util.HttpClient;
import com.yp.pay.common.util.StringUtil;
import com.yp.pay.entity.dto.*;
import com.yp.pay.entity.entity.*;
import com.yp.pay.entity.req.*;
import com.yp.pay.wx.config.CommonUtil;
import com.yp.pay.wx.config.JWellWXPayConfig;
import com.yp.pay.wx.config.WxPayProperties;
import com.yp.pay.wx.handler.WxPayHandler;
import com.yp.pay.wx.mapper.*;
import com.yp.pay.wx.service.WxPayService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @description: 微信支付实现类
 * @author: liuX
 * @time: 2020/5/26 21:30
 */
@Service
public class WxPayServiceImpl implements WxPayService {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private GlobalSysnoGenerator globalSysnoGenerator;

    @Autowired
    private TradePaymentRecordMapper tradePaymentRecordMapper;

    @Autowired
    private ChannelBillInfoMapper channelBillInfoMapper;

    @Autowired
    private MerchantPayInfoMapper merchantPayInfoMapper;

    @Autowired
    private MerchantChannelFeeMapper merchantChannelFeeMapper;

    @Autowired
    private TradeRefundRecordMapper tradeRefundRecordMapper;

    @Autowired
    private WxBillTotalInfoMapper wxBillTotalInfoMapper;

    @Autowired
    private WxPayProperties properties;

    private static final String INPUT_FORMATTER = "yyyyMMdd";

    private static final String RETURN_FORMATTER = "yyyyMMddhhmmss";

    private static final String PLAT_ORDER_PART = "yyyyMMddhhmmss";

    private static final String CHANNEL_BILL_FORMATTER = "yyyy-MM-dd HH:mm:ss";

    private final static String SUCCESS = "SUCCESS";

    private final static String FAIL = "FAIL";

    private final static String PAY_TYPE = "WX_PAY";

    private final static String PAY_NOTICE_URL = "/scanPayNotify";

    private final static String REFUND_NOTICE_URL = "/refundNotify";

    private static final String WX_JS_API = "JSAPI";

    private static final String PAYMENT_CODE = "PAYMENT_CODE";

    private static final String PROFIT_SHARE = "Y";

    /**
     * 通过商户编号获取商户配置信息
     *
     * @param merchantNo
     * @return
     * @throws BusinessException
     */
    @Override
    public MerchantInfoDTO merchantQuery(String merchantNo, String payWayCode) throws BusinessException {

        MerchantPayInfoDO merchantPayInfoDO = findMerchantByChannelAndMerchantNo(merchantNo, payWayCode);

        MerchantInfoDTO merchantInfoDTO = EntityConverter.copyAndGetSingle(merchantPayInfoDO, MerchantInfoDTO.class);

        return merchantInfoDTO;
    }

    /**
     * 通过订单号和商户编号获取订单信息
     *
     * @param merchantNo
     * @return
     * @throws BusinessException
     */
    @Override
    public TradePaymentRecordDTO getTradeOrderInfo(String merchantNo, String orderNo) throws BusinessException {

        TradePaymentRecordDO tradePaymentRecordDO = new TradePaymentRecordDO();
        // 不同平台之前的订单号可以重复（因为不同平台的APPID、秘钥、微信分配的商户号以及证书都不同）

        tradePaymentRecordDO.setMerchantNo(merchantNo);
        tradePaymentRecordDO.setOrderNo(orderNo);

        // 判断该笔订单是否已经存在 防止重复支付
        TradePaymentRecordDO tradePaymentExist = tradePaymentRecordMapper.selectRecodeByEntity(tradePaymentRecordDO);

        if (tradePaymentExist == null) {
            throw new BusinessException("未查询到到商户编号[" + merchantNo + "]下的支付订单[" + orderNo + "]，请核对请求数据。");
        }

        TradePaymentRecordDTO tradePaymentRecordDTO = EntityConverter.copyAndGetSingle(
                tradePaymentExist, TradePaymentRecordDTO.class);

        return tradePaymentRecordDTO;
    }

    /**
     * 通过code获取openid
     *
     * @param code
     * @return
     * @throws BusinessException
     */
    @Override
    public String getOpenId(String code, String merchantNo) throws BusinessException {

        JWellWXPayConfig jWellWXPayConfig = WxPayHandler.merchantInfoMap.get(merchantNo);
        if (jWellWXPayConfig == null) {
            throw new BusinessException("未获取到商户号为：" + merchantNo + "商户的相关配置信息，" +
                    "请联系相关工作人员进行商户数据确认或新增该商户" + merchantNo + "的配置信息。");
        }

        MerchantPayInfoDO merchantPayInfoDO = findMerchantByChannelAndMerchantNo(merchantNo, PAY_TYPE);
        String appId = merchantPayInfoDO.getAppId();
        String secret = merchantPayInfoDO.getRsaPrivateKey();

        StringBuffer data = new StringBuffer();
        data.append("appid=" + appId);
        data.append("&secret=" + secret);
        data.append("&code=" + code);
        data.append("&grant_type=authorization_code");
        String rev;
        try {
            logger.info("请求地址：" + properties.getOpenIdUrl() + ",请求参数：" + data);
            rev = HttpClient.post(properties.getOpenIdUrl(), data.toString(), "form");
            logger.info("接收到微信返回数据：" + rev);

        } catch (IOException e) {
            e.printStackTrace();
            throw new BusinessException("请求微信获取openid失败");
        }
        return rev;
    }

    /**
     * 作用：获取二维码链接
     *
     * @param
     * @return
     * @throws Exception
     */
    @Override
    public UnionPayCodeDTO getQrCodeInfo(QrCodeInfoReq qrCodeInfoReq) throws BusinessException {

        String merchantNo = qrCodeInfoReq.getMerchantNo();

        // 通过商户号获取商户信息，下面只是获取了商户名字
        MerchantPayInfoDO merchant = findMerchantByMerchantNo(merchantNo);

        // 不同平台之前的订单号可以重复（因为不同平台的APPID、秘钥、微信分配的商户号以及证书都不同）
        String orderNo = qrCodeInfoReq.getOrderNo();
        String subject = qrCodeInfoReq.getSubject();
        String detail = qrCodeInfoReq.getDetail();
        String attach = qrCodeInfoReq.getAttach();
        String clientIp = qrCodeInfoReq.getClientIp();
        Integer amount = qrCodeInfoReq.getAmount();


        TradePaymentRecordDO tradePaymentRecordDO = new TradePaymentRecordDO();
        tradePaymentRecordDO.setOrderNo(orderNo);
        // 判断该笔订单是否已经存在 防止重复支付
        TradePaymentRecordDO tradePaymentExist = tradePaymentRecordMapper.selectRecodeByEntity(tradePaymentRecordDO);

        if (tradePaymentExist != null) {
            throw new BusinessException("支付请求订单号[" + orderNo + "]重复，请保证每笔订单号必须唯一。");
        }

        Long sysNo = globalSysnoGenerator.nextSysno();
        tradePaymentRecordDO.setSysNo(sysNo);
        tradePaymentRecordDO.setMerchantNo(merchantNo);
        tradePaymentRecordDO.setMerchantName(merchant.getMerchantName());
        // 生成平台订单号
        String platOrderNo = merchantNo + StringUtil.getDate(PLAT_ORDER_PART) + StringUtil.generateNonceStr(4);
        tradePaymentRecordDO.setPlatOrderNo(platOrderNo);
        tradePaymentRecordDO.setVersion(1);
        tradePaymentRecordDO.setProductName(subject);
        tradePaymentRecordDO.setOrderIp(clientIp);
        tradePaymentRecordDO.setOrderAmount(amount);
        tradePaymentRecordDO.setStatus(TradeStatus.COMMIT.getCode());
        tradePaymentRecordDO.setCreateDate(new Date());
        tradePaymentRecordDO.setTradeDetail(detail);
        tradePaymentRecordDO.setTradeAttach(attach);
        tradePaymentRecordDO.setQrCodeStatus(0);
        tradePaymentRecordMapper.insertSelective(tradePaymentRecordDO);

        StringBuffer sb = new StringBuffer();
        sb.append("orderNo=" + orderNo);
        sb.append("&merchantNo=" + merchantNo);

        UnionPayCodeDTO unionPayCodeDTO = new UnionPayCodeDTO();
        unionPayCodeDTO.setOrderNo(orderNo);
        unionPayCodeDTO.setQrCodeUrl(properties.getQrCodeReturnUrl() + "?" + sb.toString());

        return unionPayCodeDTO;
    }

    /**
     * 作用：付款码支付<br>
     * 场景：客户被扫
     *
     * @param microPayReq 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    @Override
    public String microPay(WxMicroPayReq microPayReq) throws BusinessException {

        String merchantNo = microPayReq.getMerchantNo();

        JWellWXPayConfig jWellWXPayConfig = WxPayHandler.merchantInfoMap.get(merchantNo);
        if (jWellWXPayConfig == null) {
            throw new BusinessException("未获取到商户号为：" + merchantNo + "商户的相关配置信息，" +
                    "请联系相关工作人员进行商户数据确认或新增该商户" + merchantNo + "的配置信息。");
        }

        // 获取对应商户对应支付渠道和支付类型的费率信息
        Long merchantSysNo = jWellWXPayConfig.getMerchantPayInfoDO().getSysNo();
        Map<String, Object> map = new HashMap<>(16);
        map.put("merchantSysNo", merchantSysNo);
        map.put("payWayCode", PAY_TYPE);
        map.put("payTypeCode", PAYMENT_CODE);
        MerchantChannelFeeDO merchantChannelFeeDO = merchantChannelFeeMapper.selectByMap(map);
        if (merchantChannelFeeDO == null) {
            throw new BusinessException("未获取到商户号为：" + merchantNo + "商户的渠道费率配置信息。");
        }

        TradePaymentRecordDO tradePaymentRecordDO = new TradePaymentRecordDO();

        // 不同平台之前的订单号可以重复（因为不同平台的APPID、秘钥、微信分配的商户号以及证书都不同）
        String orderNo = microPayReq.getOrderNo();
        tradePaymentRecordDO.setOrderNo(orderNo);

        // 判断该笔订单是否已经存在 防止重复支付
        TradePaymentRecordDO tradePaymentExist = tradePaymentRecordMapper.selectRecodeByEntity(tradePaymentRecordDO);
        if (tradePaymentExist != null) {
            throw new BusinessException("支付请求订单号[" + orderNo + "]重复，请保证每笔订单号必须唯一。");
        }

        Long sysNo = globalSysnoGenerator.nextSysno();
        tradePaymentRecordDO.setSysNo(sysNo);
        tradePaymentRecordDO.setMerchantNo(merchantNo);
        tradePaymentRecordDO.setMerchantName(jWellWXPayConfig.merchantPayInfoDO.getMerchantName());
        tradePaymentRecordDO.setProductName(microPayReq.getSubject());
        tradePaymentRecordDO.setVersion(1);
        tradePaymentRecordDO.setOrderIp(microPayReq.getClientIp());
        // 生成平台订单号
        String platOrderNo = merchantNo + StringUtil.getDate(PLAT_ORDER_PART) + StringUtil.generateNonceStr(4);
        tradePaymentRecordDO.setPlatOrderNo(platOrderNo);
        microPayReq.setOrderNo(platOrderNo);
        Integer amount = microPayReq.getAmount();
        tradePaymentRecordDO.setOrderAmount(amount);
        tradePaymentRecordDO.setPayWayCode(PAY_TYPE);
        tradePaymentRecordDO.setPayTypeCode("付款码支付");
        tradePaymentRecordDO.setStatus(TradeStatus.COMMIT.getCode());
        tradePaymentRecordDO.setCreateDate(new Date());
        tradePaymentRecordDO.setTradeDetail(microPayReq.getDetail());
        tradePaymentRecordDO.setTradeAttach(microPayReq.getAttach());

        // 计算费率
        BigDecimal feeRate = merchantChannelFeeDO.getFeeRate();
        if (merchantChannelFeeDO.getStatus().equals(0)) {
            feeRate = new BigDecimal(0);
        }
        Integer fee = feeRate.multiply(new BigDecimal(amount)).intValue();
        tradePaymentRecordDO.setMerCost(fee);

        String profitShare = microPayReq.getProfitShare();
        // 默认不分账
        tradePaymentRecordDO.setProfitShareSign(0);
        if (PROFIT_SHARE.equals(profitShare)) {
            tradePaymentRecordDO.setProfitShareSign(1);
            tradePaymentRecordDO.setProfitShareStatus(0);
        }
        tradePaymentRecordMapper.insertSelective(tradePaymentRecordDO);

        // 将实体类对象转化成微信接口需要的Map对象
        Map reqData = CommonUtil.getMapFromObject(microPayReq, WxPayMethodType.microPay.getCode());

        Map<String, String> response = postAndReceiveData(jWellWXPayConfig, reqData, WxPayMethodType.microPay.getCode());

        TradePaymentRecordDO updateData = new TradePaymentRecordDO();
        updateData.setSysNo(sysNo);

        String return_code = response.get("return_code");
        String return_msg = response.get("return_msg");
        if (!SUCCESS.equals(return_code)) {
            updateData.setErrCode(return_code);
            updateData.setErrCodeDes(return_msg);
            tradePaymentRecordMapper.updateRecodeByInput(updateData);
            throw new BusinessException(return_msg);
        }

        String result_code = response.get("result_code");
        String err_code = response.get("err_code");
        String err_code_des = response.get("err_code_des");
        if (!SUCCESS.equals(result_code)) {
            updateData.setErrCode(err_code);
            updateData.setErrCodeDes(err_code_des);

            tradePaymentRecordMapper.updateRecodeByInput(updateData);
            throw new BusinessException(err_code_des);
        }

        String transaction_id = response.get("transaction_id");//微信支付订单号
        String time_end = response.get("time_end");//支付完成时间
        Date date = StringUtil.formatDateValue(time_end, RETURN_FORMATTER);

        updateData.setPaySuccessTime(date);
        updateData.setStatus(TradeStatus.SUCCESS.getCode());
        updateData.setChannelOrderNo(transaction_id);
        tradePaymentRecordMapper.updateRecodeByInput(updateData);

        return platOrderNo;
    }

    /**
     * 作用：统一支付接口<br>
     * 场景：
     * 1、客户主扫NATIVE
     * 2、JSAPI 获取预支付ID，前端页面调用发起支付
     * 3、APP支付
     *
     * @param wxUnifiedPayReq 向wxpay post的请求数据
     * @return
     * @throws Exception
     */
    @Override
    public ScanCodeDTO unifiedPay(WxUnifiedPayReq wxUnifiedPayReq) throws BusinessException {

        String merchantNo = wxUnifiedPayReq.getMerchantNo();

        JWellWXPayConfig jWellWXPayConfig = WxPayHandler.merchantInfoMap.get(merchantNo);
        if (jWellWXPayConfig == null) {
            throw new BusinessException("未获取到商户号为：" + merchantNo + "商户的相关配置信息，" +
                    "请联系相关工作人员进行商户数据确认或新增该商户" + merchantNo + "的配置信息。");
        }

        TradePaymentRecordDO tradePaymentRecordDO = new TradePaymentRecordDO();

        // 不同平台之前的订单号可以重复（因为不同平台的APPID、秘钥、微信分配的商户号以及证书都不同）
        String orderNo = wxUnifiedPayReq.getOrderNo();
        tradePaymentRecordDO.setOrderNo(orderNo);

        TradePaymentRecordDO updateData = new TradePaymentRecordDO();
        String tradeType = wxUnifiedPayReq.getTradeType();
        updateData.setPayTypeCode(tradeType);

        // 获取对应商户对应支付渠道和支付类型的费率信息
        Long merchantSysNo = jWellWXPayConfig.getMerchantPayInfoDO().getSysNo();
        Map<String, Object> map = new HashMap<>(16);
        map.put("merchantSysNo", merchantSysNo);
        map.put("payWayCode", PAY_TYPE);
        map.put("payTypeCode", tradeType);
        MerchantChannelFeeDO merchantChannelFeeDO = merchantChannelFeeMapper.selectByMap(map);
        if (merchantChannelFeeDO == null) {
            throw new BusinessException("未获取到商户号为：" + merchantNo + "商户的" + PAY_TYPE + "支付渠道" + tradeType + "支付方式费率配置信息。");
        }

        TradePaymentRecordDO tradePaymentExist = tradePaymentRecordMapper.selectRecodeByEntity(tradePaymentRecordDO);
        Long sysNo = globalSysnoGenerator.nextSysno();

        // 生成平台订单号
        String platOrderNo = merchantNo + StringUtil.getDate(PLAT_ORDER_PART) + StringUtil.generateNonceStr(4);
        tradePaymentRecordDO.setPlatOrderNo(platOrderNo);

        Boolean aggregationPay = wxUnifiedPayReq.getAggregationPay();
        if (WX_JS_API.equals(tradeType) && aggregationPay) {

            // 如果是jspay,在扫码的时候已经将订单存入数据库
            if (tradePaymentExist == null) {
                throw new BusinessException("支付请求订单号[" + orderNo + "]在订单表中并不存在，请核对请求订单信息。");
            }

            sysNo = tradePaymentExist.getSysNo();
            Integer status = tradePaymentExist.getStatus();
            // 满足数码仓，支付中的状态可以进行支付
//            if (!AliAndWXPayStatus.SUBMITTED.getCode().equals(status)) {
//                throw new BusinessException("订单号[" + orderNo + "]的订单已经提交支付，不能重复发起支付。");
//            }
            if (TradeStatus.SUCCESS.getCode().equals(status)) {
                throw new BusinessException("订单号[" + orderNo + "]的订单已经支付成功，不能重复发起支付。");
            }
            if (TradeStatus.FAIL.getCode().equals(status)) {
                throw new BusinessException("订单号[" + orderNo + "]的订单已经支付失败，不能重复发起支付。");
            }
            if (TradeStatus.CLOSED.getCode().equals(status)) {
                throw new BusinessException("订单号[" + orderNo + "]的订单已经关闭，请重新下单生成二维码进行支付。");
            }
            Integer qrCodeStatus = tradePaymentExist.getQrCodeStatus();
            if (qrCodeStatus != null && qrCodeStatus == 3) {
                throw new BusinessException("该订单已被取消，不能支付，请重新生成二维码。");
            }

            Integer amount = tradePaymentExist.getOrderAmount();
            // 计算费率
            BigDecimal feeRate = merchantChannelFeeDO.getFeeRate();
            if (merchantChannelFeeDO.getStatus().equals(0)) {
                feeRate = new BigDecimal(0);
            }
            Integer fee = feeRate.multiply(new BigDecimal(amount)).intValue();
            updateData.setMerCost(fee);

            updateData.setQrCodeStatus(1);
            updateData.setProfitShareSign(0);
            String profitShare = wxUnifiedPayReq.getProfitShare();
            if (PROFIT_SHARE.equals(profitShare)) {
                updateData.setProfitShareSign(1);
                updateData.setProfitShareStatus(0);
            }

            updateData.setPlatOrderNo(platOrderNo);

        } else {
            // 判断该笔订单是否已经存在 防止重复支付
            if (tradePaymentExist != null) {
                throw new BusinessException("支付请求订单号[" + orderNo + "]重复，请保证每笔订单号必须唯一。");
            }
            tradePaymentRecordDO.setSysNo(sysNo);
            tradePaymentRecordDO.setMerchantNo(merchantNo);
            tradePaymentRecordDO.setMerchantName(jWellWXPayConfig.merchantPayInfoDO.getMerchantName());
            tradePaymentRecordDO.setVersion(1);
            tradePaymentRecordDO.setProductName(wxUnifiedPayReq.getSubject());
            tradePaymentRecordDO.setOrderIp(wxUnifiedPayReq.getClientIp());
            Integer amount = wxUnifiedPayReq.getAmount();
            tradePaymentRecordDO.setOrderAmount(amount);
            tradePaymentRecordDO.setPlatOrderNo(platOrderNo);
            tradePaymentRecordDO.setPayWayCode(PAY_TYPE);
            tradePaymentRecordDO.setPayTypeCode(tradeType);
            tradePaymentRecordDO.setStatus(TradeStatus.COMMIT.getCode());
            tradePaymentRecordDO.setCreateDate(new Date());
            tradePaymentRecordDO.setTradeDetail(wxUnifiedPayReq.getDetail());
            tradePaymentRecordDO.setTradeAttach(wxUnifiedPayReq.getAttach());

            // 计算费率
            BigDecimal feeRate = merchantChannelFeeDO.getFeeRate();
            if (merchantChannelFeeDO.getStatus().equals(0)) {
                feeRate = new BigDecimal(0);
            }
            Integer fee = feeRate.multiply(new BigDecimal(amount)).intValue();
            tradePaymentRecordDO.setMerCost(fee);

            String profitShare = wxUnifiedPayReq.getProfitShare();
            // 默认不分账
            tradePaymentRecordDO.setProfitShareSign(0);
            if (PROFIT_SHARE.equals(profitShare)) {
                tradePaymentRecordDO.setProfitShareSign(1);
                tradePaymentRecordDO.setProfitShareStatus(0);
            }
            tradePaymentRecordMapper.insertSelective(tradePaymentRecordDO);
        }

        wxUnifiedPayReq.setOrderNo(platOrderNo);
        // 将实体类对象转化成Map对象
        Map reqData = CommonUtil.getMapFromObject(wxUnifiedPayReq, WxPayMethodType.scanPay.getCode());
        reqData.put("notify_url", jWellWXPayConfig.merchantPayInfoDO.getNotifyUrl() + PAY_NOTICE_URL);

        Map<String, String> response = postAndReceiveData(jWellWXPayConfig, reqData, WxPayMethodType.scanPay.getCode());

        updateData.setSysNo(sysNo);
        updateData.setStatus(TradeStatus.HANDING.getCode());//状态修改为处理中

        String return_code = response.get("return_code");
        String return_msg = response.get("return_msg");
        if (!SUCCESS.equals(return_code)) {
            updateData.setErrCode(return_code);
            updateData.setErrCodeDes(return_msg);
            tradePaymentRecordMapper.updateRecodeByInput(updateData);
            throw new BusinessException(return_msg);
        }

        String result_code = response.get("result_code");
        String err_code = response.get("err_code");
        String err_code_des = response.get("err_code_des");
        if (!SUCCESS.equals(result_code)) {

            updateData.setErrCode(err_code);
            updateData.setErrCodeDes(err_code_des);

            tradePaymentRecordMapper.updateRecodeByInput(updateData);
            throw new BusinessException(err_code_des);
        }

        //预支付交易会话标识 微信生成的预支付会话标识，用于后续接口调用中使用，该值有效期为2小时
        String prepay_id = response.get("prepay_id");
        //二维码链接 trade_type=NATIVE时有返回，此url用于生成支付二维码，然后提供给用户进行扫码支付。
        String code_url = response.get("code_url");

        updateData.setRemark("prepay_id:" + prepay_id + ",code_url:" + code_url);// 将二维码信息存入数据库
        //将数据存入数据库
        tradePaymentRecordMapper.updateRecodeByInput(updateData);

        ScanCodeDTO scanCodeDTO = new ScanCodeDTO();
        scanCodeDTO.setOrderNo(orderNo);
        scanCodeDTO.setPrepayId(prepay_id);
        scanCodeDTO.setCodeUrl(code_url);

        return scanCodeDTO;
    }

    @Override
    public WxAppPayDTO appPay(WxUnifiedPayReq wxUnifiedPayReq) throws BusinessException {

        String merchantNo = wxUnifiedPayReq.getMerchantNo();

        JWellWXPayConfig jWellWXPayConfig = WxPayHandler.merchantInfoMap.get(merchantNo);
        if (jWellWXPayConfig == null) {
            throw new BusinessException("未获取到商户号为：" + merchantNo + "商户的相关配置信息，" +
                    "请联系相关工作人员进行商户数据确认或新增该商户" + merchantNo + "的配置信息。");
        }

        ScanCodeDTO scanCodeDTO = this.unifiedPay(wxUnifiedPayReq);

        WxAppPayDTO wxAppPayDTO = new WxAppPayDTO();

        String prepayId = scanCodeDTO.getPrepayId();
        wxAppPayDTO.setPrepayId(prepayId);

        wxAppPayDTO.setOrderNo(scanCodeDTO.getOrderNo());

        String appID = jWellWXPayConfig.getAppID();
        wxAppPayDTO.setAppId(appID);

        String mchID = jWellWXPayConfig.getMchID();
        wxAppPayDTO.setMerId(mchID);

        String nonceStr = WXPayUtil.generateNonceStr();
        wxAppPayDTO.setNonceStr(nonceStr);

        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        wxAppPayDTO.setTimeStamp(timeStamp);

        Map<String, String> map = new HashMap();
        map.put("appid", appID);
        map.put("partnerid", mchID);
        map.put("prepayid", prepayId);
        map.put("package", "Sign=WXPay");
        map.put("noncestr", nonceStr);
        map.put("timestamp", timeStamp);

        try {
            String sign = WXPayUtil.generateSignature(map, jWellWXPayConfig.getKey(), WXPayConstants.SignType.HMACSHA256);
            wxAppPayDTO.setSign(sign);
        } catch (Exception e) {
            e.getStackTrace();
            throw new BusinessException("微信加密异常，请检查相关代码");
        }

        return wxAppPayDTO;
    }

    /**
     * 作用：查询订单<br>
     * 场景：付款码支付、公共号支付、扫码支付、APP支付
     * <p>
     *
     * @param orderQueryOrReverseReq 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    @Override
    public TradePaymentRecordDTO orderQuery(WxOrderQueryOrReverseReq orderQueryOrReverseReq) throws BusinessException {

        String merchantNo = orderQueryOrReverseReq.getMerchantNo();

        JWellWXPayConfig jWellWXPayConfig = WxPayHandler.merchantInfoMap.get(merchantNo);
        if (jWellWXPayConfig == null) {
            throw new BusinessException("未获取到商户号为：" + merchantNo + "商户的相关配置信息，" +
                    "请联系相关工作人员进行商户数据确认或新增该商户" + merchantNo + "的配置信息。");
        }

        TradePaymentRecordDO tradePaymentRecordDO = new TradePaymentRecordDO();

        String orderNo = orderQueryOrReverseReq.getOrderNo();
        if (StringUtils.isNotBlank(orderNo)) {
            tradePaymentRecordDO.setOrderNo(orderNo);
        }

        String platOrderNo = orderQueryOrReverseReq.getPlatOrderNo();
        if (StringUtils.isNotBlank(platOrderNo)) {
            tradePaymentRecordDO.setPlatOrderNo(platOrderNo);
        }

        // 查询该笔订单是否存在
        TradePaymentRecordDO tradePaymentExist = tradePaymentRecordMapper.selectRecodeByEntity(tradePaymentRecordDO);
        if (tradePaymentExist == null) {
            throw new BusinessException("数据库无法查询到该笔交易订单，请核实订单号是否输入正确或者输入是否匹配。");
        }

        orderQueryOrReverseReq.setChannelOrderNo(tradePaymentExist.getChannelOrderNo());

        TradePaymentRecordDTO queryTradeRecord = EntityConverter.copyAndGetSingle(tradePaymentExist, TradePaymentRecordDTO.class);
        if (!merchantNo.equals(queryTradeRecord.getMerchantNo())) {
            throw new BusinessException("该订单并非该商户的交易订单，请核实数据。");
        }

        // 将实体类对象转化成Map对象
        Map reqData = CommonUtil.getMapFromObject(orderQueryOrReverseReq, WxPayMethodType.orderQuery.getCode());

        // 只有无订单状态、0：已提交、1：处理中和5：退款中的订单 需要到微信进行查询核实订单状态
        if (queryTradeRecord.getStatus() == null ||
                TradeStatus.COMMIT.getCode().equals(queryTradeRecord.getStatus()) ||
                TradeStatus.HANDING.getCode().equals(queryTradeRecord.getStatus())) {

            Map<String, String> response = postAndReceiveData(jWellWXPayConfig, reqData, WxPayMethodType.orderQuery.getCode());

            TradePaymentRecordDO updateData = new TradePaymentRecordDO();
            updateData.setSysNo(queryTradeRecord.getSysNo());

            String return_code = response.get("return_code");
            String return_msg = response.get("return_msg");
            if (!SUCCESS.equals(return_code)) {
                updateData.setErrCode(return_code);
                updateData.setErrCodeDes(return_msg);
                tradePaymentRecordMapper.updateRecodeByInput(updateData);
                throw new BusinessException(return_msg);
            }

            // TODO 貌似不需要验签
            String sign = response.get("sign");
            String returnSign;
            try {
                returnSign = WXPayUtil.generateSignature(response, jWellWXPayConfig.getKey(), WXPayConstants.SignType.HMACSHA256);
            } catch (Exception e) {
                e.printStackTrace();
                throw new BusinessException("微信订单状态查询返回数据验签异常，请查看日志");
            }
            if (sign.equals(returnSign)) {

                String result_code = response.get("result_code");
                String err_code = response.get("err_code");
                String err_code_des = response.get("err_code_des");

                updateData.setErrCode(err_code);
                if (!SUCCESS.equals(result_code)) {
                    updateData.setErrCodeDes(err_code_des);

                    tradePaymentRecordMapper.updateRecodeByInput(updateData);
                    throw new BusinessException(err_code_des);
                }

                /**
                 * 除了成功失败未支付状态，还需要有其他状态，如已退款等 TODO
                 * 具体状态有如下几种
                 * SUCCESS—支付成功 REFUND—转入退款 NOTPAY—未支付 CLOSED—已关闭 REVOKED—已撤销（付款码支付）
                 * USERPAYING--用户支付中（付款码支付） PAYERROR--支付失败(其他原因，如银行返回失败)
                 */
                String trade_state = response.get("trade_state");
                String trade_state_desc = response.get("trade_state_desc");//对当前查询订单状态的描述和下一步操作的指引
                String transaction_id = response.get("transaction_id");//微信支付订单号
                String time_end = response.get("time_end");//支付完成时间

                if (!SUCCESS.equals(trade_state)) {

                    if ("USERPAYING".equals(trade_state)) {
                        updateData.setStatus(TradeStatus.HANDING.getCode());
                    } else if ("CLOSED".equals(trade_state)) {
                        updateData.setStatus(TradeStatus.CLOSED.getCode());
                    } else {
                        updateData.setStatus(TradeStatus.FAIL.getCode());
                        updateData.setErrCode(trade_state);
                    }

                    String detailDesc = "";
                    if (StringUtils.isNotBlank(trade_state_desc) || trade_state_desc.length() > 0) {
                        detailDesc = "," + trade_state_desc;
                    }

                    String errDesc = WxPayHandler.payResultMap.get(trade_state) + detailDesc;
                    updateData.setErrCodeDes(errDesc);
                    tradePaymentRecordMapper.updateRecodeByInput(updateData);

                    throw new BusinessException(errDesc);
                }

                updateData.setStatus(TradeStatus.SUCCESS.getCode());
                updateData.setChannelOrderNo(transaction_id);

                Date date = StringUtil.formatDateValue(time_end, RETURN_FORMATTER);
                updateData.setPaySuccessTime(date);

                tradePaymentRecordMapper.updateRecodeByInput(updateData);

                queryTradeRecord.setStatus(TradeStatus.SUCCESS.getCode());
                queryTradeRecord.setPaySuccessTime(date);

                return queryTradeRecord;
            }
            logger.error("微信订单状态查询返回数据验签失败，请核实数据是否被篡改。");
        }
        // 如果订单状态为成功，则支付返回
        return queryTradeRecord;
    }

    /**
     * 作用：关闭订单<br>
     * 场景：统一支付
     *
     * @param wxCloseOrderReq 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    @Override
    public CloseOrderDTO closeOrder(WxCloseOrderReq wxCloseOrderReq) throws BusinessException {
        // 判断请求参数是否为空
        String orderNo = wxCloseOrderReq.getOrderNo();

        TradePaymentRecordDO tradePaymentRecordDO = new TradePaymentRecordDO();
        tradePaymentRecordDO.setOrderNo(orderNo);

        // 查询该笔订单是否存在
        TradePaymentRecordDO tradePaymentExist = tradePaymentRecordMapper.selectRecodeByEntity(tradePaymentRecordDO);
        if (tradePaymentExist == null) {
            throw new BusinessException("数据库无法查询到该笔交易订单，请核实订单号是否输入正确。");
        }

        Long sysNo = tradePaymentExist.getSysNo();

        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setOrderNo(orderNo);
        closeOrderDTO.setResultCode(FAIL);

        TradePaymentRecordDO updateData = new TradePaymentRecordDO();
        updateData.setSysNo(sysNo);

        //  只有无订单状态、0：已提交和1：处理中的订单可以进行关闭操作
        if (TradeStatus.HANDING.getCode().equals(tradePaymentExist.getStatus())) {

            // 将实体类对象转化成Map对象
            Map reqData = CommonUtil.getMapFromObject(wxCloseOrderReq, WxPayMethodType.closeOrder.getCode());

            String merchantNo = wxCloseOrderReq.getMerchantNo();

            JWellWXPayConfig jWellWXPayConfig = WxPayHandler.merchantInfoMap.get(merchantNo);
            if (jWellWXPayConfig == null) {
                throw new BusinessException("未获取到商户号为：" + merchantNo + "商户的相关配置信息，" +
                        "请联系相关工作人员进行商户数据确认或新增该商户" + merchantNo + "的配置信息。");
            }

            Map<String, String> response = postAndReceiveData(jWellWXPayConfig, reqData, WxPayMethodType.closeOrder.getCode());

            String return_code = response.get("return_code");
            String return_msg = response.get("return_msg");
            if (!SUCCESS.equals(return_code)) {
                updateData.setErrCode(return_code);
                updateData.setErrCodeDes(return_msg);
                closeOrderDTO.setResultCode(return_msg);
                tradePaymentRecordMapper.updateRecodeByInput(updateData);
                throw new BusinessException(return_msg);
            }

            String result_code = response.get("result_code");
            String result_msg = response.get("result_msg");
            String err_code = response.get("err_code");
            String err_code_des = response.get("err_code_des");
            if (!SUCCESS.equals(result_code)) {

                updateData.setErrCode(err_code);
                updateData.setErrCodeDes(err_code_des);
                closeOrderDTO.setResultCode(err_code_des);

                tradePaymentRecordMapper.updateRecodeByInput(updateData);
                throw new BusinessException(err_code_des);
            }
            closeOrderDTO.setResultCode(SUCCESS);

            //状态修改为交易关闭
            updateData.setStatus(TradeStatus.CLOSED.getCode());

            //将数据存入数据库
            tradePaymentRecordMapper.updateRecodeByInput(updateData);

        }
        if (TradeStatus.COMMIT.getCode().equals(tradePaymentExist.getStatus())) {
            updateData.setStatus(TradeStatus.CLOSED.getCode());
            updateData.setModifyUser("平台关闭订单");
            closeOrderDTO.setResultCode(SUCCESS);
            tradePaymentRecordMapper.updateRecodeByInput(updateData);
        } else {
            closeOrderDTO.setResultMsg("该笔订单状态（支付完成或者为已关闭）不能进行关闭操作，请查看该笔订单的状态。");
        }

        return closeOrderDTO;
    }

    /**
     * 作用：撤销订单<br>
     * 场景：付款码支付
     *
     * @param orderQueryOrReverseReq 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    @Override
    public Map<String, String> reverse(WxOrderQueryOrReverseReq orderQueryOrReverseReq) throws BusinessException {

        // TODO 先查询数据

        // 将实体类对象转化成Map对象
        Map reqData = CommonUtil.getMapFromObject(orderQueryOrReverseReq, WxPayMethodType.reverse.getCode());

        String merchantNo = orderQueryOrReverseReq.getMerchantNo();

        JWellWXPayConfig jWellWXPayConfig = WxPayHandler.merchantInfoMap.get(merchantNo);
        if (jWellWXPayConfig == null) {
            throw new BusinessException("未获取到商户号为：" + merchantNo + "商户的相关配置信息，" +
                    "请联系相关工作人员进行商户数据确认或新增该商户" + merchantNo + "的配置信息。");
        }

        return postAndReceiveData(jWellWXPayConfig, reqData, WxPayMethodType.reverse.getCode());
    }

    /**
     * 作用：申请退款<br>
     * 场景：付款码支付、公共号支付、扫码支付、APP支付
     * TODO
     *
     * @param refundReq 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    @Override
    public ApplyRefundDTO refund(WxRefundReq refundReq) throws BusinessException {

        String merchantNo = refundReq.getMerchantNo();

        JWellWXPayConfig jWellWXPayConfig = WxPayHandler.merchantInfoMap.get(merchantNo);
        if (jWellWXPayConfig == null) {
            throw new BusinessException("未获取到商户号为：" + merchantNo + "商户的相关配置信息，" +
                    "请联系相关工作人员进行商户数据确认或新增该商户" + merchantNo + "的配置信息。");
        }

        ApplyRefundDTO applyRefundDTO = new ApplyRefundDTO();

        // 判断当前退款的该笔订单是否存在
        Map<String, Object> map = new HashMap<>(16);

        String orderNo = refundReq.getOriginalOrderNo();
        if (StringUtils.isNotBlank(orderNo)) {
            map.put("merchantOrderNo", orderNo);
        }

        String platOrderNo = refundReq.getOriginalPlatOrderNo();
        if (StringUtils.isNotBlank(platOrderNo)) {
            map.put("platOrderNo", platOrderNo);
        }

        // 查询数据库该笔订单是否存在
        TradePaymentRecordDO tradePaymentRecordDO = tradePaymentRecordMapper.selectRecodeByMap(map);
        if (tradePaymentRecordDO == null) {
            throw new BusinessException("数据库无法查询到该笔交易订单，请核实订单号是否输入正确。");
        }

        // 查询是否存在该笔退款订单号的数据
        String refundNo = refundReq.getRefundNo();
        TradeRefundRecordDO tradeRefundRecordDO = tradeRefundRecordMapper.selectRefundByRefundNo(refundNo);
        if (tradeRefundRecordDO != null) {
            throw new BusinessException("退款单号重复，您输入的该笔退款单号[" + refundNo + "]的退款数据已经存在，请重新输入");
        }

        String channelOrderNo = tradePaymentRecordDO.getChannelOrderNo();
        refundReq.setOriginalChannelOrderNo(channelOrderNo);

        // 退款金额不能大于订单金额
        Integer refundAmount = refundReq.getRefundAmount();
        Integer orderAmount = tradePaymentRecordDO.getOrderAmount();
        if (refundAmount > orderAmount) {
            throw new BusinessException("退款金额不能大于原订单金额，请核对退款金额数据。");
        }

        // 生成平台退款订单号
        String platRefundNo = merchantNo + StringUtil.getDate(PLAT_ORDER_PART) + StringUtil.generateNonceStr(2);
        refundReq.setRefundNo(platRefundNo);

        // 将实体类对象转化成Map对象
        Map reqData = CommonUtil.getMapFromObject(refundReq, WxPayMethodType.refund.getCode());
        reqData.put("total_fee", orderAmount);

        // 只有支付成功的订单且该订单才可以发起退款请求
        if (tradePaymentRecordDO.getStatus() != null &&
                TradeStatus.SUCCESS.getCode().equals(tradePaymentRecordDO.getStatus())) {

            // 如果已经发生退款且全额退款完成则无法再次申请退款
            if (tradePaymentRecordDO.getRefundStatus().equals(PayRefundStatus.REFUND_ALL.getCode())) {
                throw new BusinessException("该笔订单已经全部退款完成，无法再次退款，请核对订单。");
            }

            reqData.put("notify_url", jWellWXPayConfig.merchantPayInfoDO.getNotifyUrl() + REFUND_NOTICE_URL);

            Map<String, String> response = postAndReceiveData(jWellWXPayConfig, reqData, WxPayMethodType.refund.getCode());

            tradeRefundRecordDO = new TradeRefundRecordDO();
            Long sysNo = globalSysnoGenerator.nextSysno();
            tradeRefundRecordDO.setSysNo(sysNo);
            tradeRefundRecordDO.setMerchantNo(merchantNo);
            // 商户名
            tradeRefundRecordDO.setMerchantName(jWellWXPayConfig.getMerchantPayInfoDO().getMerchantName());
            tradeRefundRecordDO.setVersion(1);
            tradeRefundRecordDO.setOrderNo(orderNo);
            tradeRefundRecordDO.setPlatOrderNo(tradePaymentRecordDO.getPlatOrderNo());
            tradeRefundRecordDO.setChannelOrderNo(channelOrderNo);
            tradeRefundRecordDO.setOrderAmount(tradePaymentRecordDO.getOrderAmount());
            tradeRefundRecordDO.setPayWayCode(tradePaymentRecordDO.getPayWayCode());
            tradeRefundRecordDO.setRefundNo(refundNo);
            tradeRefundRecordDO.setPlatRefundNo(platRefundNo);
            tradeRefundRecordDO.setStatus(RefundStatus.REFUND_APPLY.getCode());
            tradeRefundRecordDO.setRefundApplyTime(new Date());
            tradeRefundRecordDO.setRefundApplyAmount(refundReq.getRefundAmount());
            tradeRefundRecordDO.setPayTypeCode(tradePaymentRecordDO.getPayTypeCode());
            // 退款手续费暂定为0元 TODO
            tradeRefundRecordDO.setMerCost(0);
            int i = tradeRefundRecordMapper.insertSelective(tradeRefundRecordDO);
            if (i < 1) {
                logger.error("退款数据未能成功存入数据库中，请手动处理。【" + tradeRefundRecordDO.toString() + "】");
            }

            TradePaymentRecordDO updatePayment = new TradePaymentRecordDO();
            updatePayment.setSysNo(tradePaymentRecordDO.getSysNo());
            if (refundAmount.equals(orderAmount)) {
                updatePayment.setRefundStatus(PayRefundStatus.REFUND_ALL.getCode());
                updatePayment.setRefundTimes(1);
            } else {
                updatePayment.setRefundStatus(PayRefundStatus.REFUND_PART.getCode());
                Integer refundTimes = tradePaymentRecordDO.getRefundTimes();
                refundTimes = refundTimes == null ? 0 : refundTimes;
                // 设置退款次数加一
                updatePayment.setRefundTimes(refundTimes + 1);
            }

            String return_code = response.get("return_code");
            String return_msg = response.get("return_msg");
            applyRefundDTO.setResultCode(return_code);

            TradeRefundRecordDO updateRefund = new TradeRefundRecordDO();
            updateRefund.setSysNo(sysNo);
            if (!SUCCESS.equals(return_code)) {

                updateRefund.setErrCode(return_code);
                updateRefund.setErrCodeDes(return_msg);
                tradeRefundRecordMapper.updateRefundByInput(updateRefund);

                throw new BusinessException(return_msg);
            }

            // 业务结果 SUCCESS/FAIL
            String result_code = response.get("result_code");
            applyRefundDTO.setResultCode(result_code);
            String err_code = response.get("err_code");
            String err_code_des = response.get("err_code_des");

            applyRefundDTO.setErrCode(err_code);
            if (!SUCCESS.equals(result_code)) {

                updateRefund.setErrCode(err_code);
                updateRefund.setErrCodeDes(err_code_des);
                tradeRefundRecordMapper.updateRefundByInput(updateRefund);

                throw new BusinessException(err_code_des);
            }

            // 设置状态为退款中
            updateRefund.setStatus(RefundStatus.REFUNDING.getCode());

            //微信订单号
            String transaction_id = response.get("transaction_id");
            applyRefundDTO.setTransactionId(transaction_id);
            //商户订单号
            String out_trade_no = response.get("out_trade_no");
            applyRefundDTO.setOrderNo(out_trade_no);
            //商户退款单号
            String out_refund_no = response.get("out_refund_no");
            applyRefundDTO.setRefundOrderNo(out_refund_no);
            //微信退款单号
            String refund_id = response.get("refund_id");
            applyRefundDTO.setRefundId(refund_id);
            //退款金额
            String refund_fee = response.get("refund_fee");
            applyRefundDTO.setRefundFee(new Integer(refund_fee));
            //订单金额
            String total_fee = response.get("total_fee");
            applyRefundDTO.setTotalFee(new Integer(total_fee));
            //货币类型 默认人民币：CNY
            String fee_type = response.get("fee_type");
            applyRefundDTO.setFeeType(fee_type);
            //现金支付金额
            String cash_fee = response.get("cash_fee");
            applyRefundDTO.setCashFee(new Integer(cash_fee));

            updateRefund.setPlatRefundNo(out_refund_no);
            updateRefund.setChannelRefundNo(refund_id);
            int refundRecord = tradeRefundRecordMapper.updateRefundByInput(updateRefund);
            if (refundRecord < 1) {
                logger.error("请求退款成功，但是记录数据到数据库失败，请手动处理。[" + updateRefund.toString() + "]");
            }

            int paymentRecord = tradePaymentRecordMapper.updateRecodeByInput(updatePayment);
            if (paymentRecord < 1) {
                logger.error("请求退款成功，但是更新退款状态到支付订单数据库失败，请手动处理。[" + updatePayment.toString() + "]");
            }

            return applyRefundDTO;
        } else {
            // 如果订单状态为成功，则支付返回
            applyRefundDTO.setResultCode("FAIL");
            applyRefundDTO.setErrCodeMsg("该笔订单支付状态不能退款，只有支付成功的订单可以申请退款。请核实该订单状态。");
            return applyRefundDTO;
        }

    }

    /**
     * 作用：退款查询<br>
     * 场景：付款码支付、公共号支付、扫码支付、APP支付
     * TODO 后期需要新建退款数据表，一笔支付订单可以分多笔退款单
     *
     * @param refundQueryReq 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    @Override
    public RefundQueryDTO refundQuery(WxRefundQueryReq refundQueryReq) throws BusinessException {

        String merchantNo = refundQueryReq.getMerchantNo();
        JWellWXPayConfig jWellWXPayConfig = WxPayHandler.merchantInfoMap.get(merchantNo);
        if (jWellWXPayConfig == null) {
            throw new BusinessException("未获取到商户号为：" + merchantNo + "商户的相关配置信息，" +
                    "请联系相关工作人员进行商户数据确认或新增该商户" + merchantNo + "的配置信息。");
        }

        /**
         * 1、查询退款钱先进行订单查询，判断该笔订单是否存在
         * 2、退款中或者退款失败的订单可以进行查询
         * 3、更新查询结果
         * 4、返回查询结果
         */
        // 判断当前退款的该笔订单是否存在
        TradeRefundRecordDO tradeRefundRecordDO = new TradeRefundRecordDO();

        String refundNo = refundQueryReq.getRefundNo();
        if (StringUtils.isNotBlank(refundNo)) {
            tradeRefundRecordDO.setRefundNo(refundNo);
        }

        String platRefundNo = refundQueryReq.getPlatRefundNo();
        if (StringUtils.isNotBlank(platRefundNo)) {
            tradeRefundRecordDO.setPlatRefundNo(platRefundNo);
        }

        // 查询数据库该笔订单是否存在
        TradeRefundRecordDO refundRecordDO = tradeRefundRecordMapper.selectOne(tradeRefundRecordDO);

        if (refundRecordDO == null) {
            throw new BusinessException("数据库无法查询到该笔交易订单，请核实订单号是否输入正确。");
        }

        RefundQueryDTO refundQueryDTO = new RefundQueryDTO();
        List<RefundQueryDetailDTO> refundQueryDetailDTOS = new ArrayList<>();

        refundQueryReq.setPlatOrderNo(refundRecordDO.getPlatOrderNo());
        refundQueryReq.setChannelOrderNo(refundRecordDO.getChannelOrderNo());
        refundQueryReq.setChannelRefundNo(refundRecordDO.getChannelRefundNo());
        // 将实体类对象转化成Map对象
        Map reqData = CommonUtil.getMapFromObject(refundQueryReq, WxPayMethodType.refundQuery.getCode());

        // 只有退款中和退款失败的订单查
        if (RefundStatus.REFUNDING.getCode().equals(refundRecordDO.getStatus()) ||
                RefundStatus.REFUND_FAIL.getCode().equals(refundRecordDO.getStatus())) {

            Map<String, String> response = postAndReceiveData(jWellWXPayConfig, reqData, WxPayMethodType.refundQuery.getCode());

            // 同时跟新支付数据表和退款信息表 TODO 通知接口没有处理错误返回
            TradeRefundRecordDO updateData = new TradeRefundRecordDO();
            updateData.setSysNo(refundRecordDO.getSysNo());

            String return_code = response.get("return_code");
            String return_msg = response.get("return_msg");
            refundQueryDTO.setResultCode(return_code);
            if (!SUCCESS.equals(return_code)) {

                updateData.setErrCode(return_code);
                updateData.setErrCodeDes(return_msg);

                tradeRefundRecordMapper.updateRefundByInput(updateData);
                throw new BusinessException(return_msg);
            }

            // 业务结果 SUCCESS/FAIL
            String result_code = response.get("result_code");
            refundQueryDTO.setResultCode(result_code);
            String err_code = response.get("err_code");
            String err_code_des = response.get("err_code_des");

            updateData.setErrCode(err_code);
            refundQueryDTO.setErrCode(err_code);
            if (!SUCCESS.equals(result_code)) {
                updateData.setErrCodeDes(err_code_des);

                tradeRefundRecordMapper.updateRefundByInput(updateData);
                throw new BusinessException(err_code_des);
            }
            // 设置状态为退款成功
            updateData.setStatus(RefundStatus.REFUND_SUCCESS.getCode());

            //微信订单号
            String transaction_id = response.get("transaction_id");
            updateData.setChannelOrderNo(transaction_id);
            refundQueryDTO.setOriginalOrderNo(refundRecordDO.getOrderNo());
            //商户订单号
            String out_trade_no = response.get("out_trade_no");
            refundQueryDTO.setOriginalPlatOrderNo(out_trade_no);
            //订单金额
            String total_fee = response.get("total_fee");
            refundQueryDTO.setTotalFee(new Integer(total_fee));
            //现金支付金额
            String cash_fee = response.get("cash_fee");
            refundQueryDTO.setCashFee(new Integer(cash_fee));
            //退款笔数
            Integer refund_count = Integer.parseInt(response.get("refund_count"));
            if (refund_count > 0) {
                String[] key = new String[]{"out_refund_no_", "refund_id_",
                        "refund_fee_", "settlement_refund_fee_", "refund_status_",
                        "refund_recv_accout_", "refund_success_time_"};

                // TODO 后期需要改进，目前只支持退款笔数refund_count1,若果一笔订单分多笔退款则无法支持
                for (int i = 0; i < refund_count; i++) {
                    RefundQueryDetailDTO refundQueryDetailDTO = new RefundQueryDetailDTO();

                    String refundOrderNoReturn = response.get(key[0] + "i");
                    refundQueryDetailDTO.setPlatRefundNo(refundOrderNoReturn);
                    updateData.setPlatRefundNo(refundOrderNoReturn);

                    String channelRefundOrderNoReturn = response.get(key[1] + "i");
                    refundQueryDetailDTO.setChannelRefundNo(channelRefundOrderNoReturn);
                    updateData.setChannelRefundNo(channelRefundOrderNoReturn);

                    refundQueryDetailDTO.setRefundFee(new Integer(response.get(key[2] + "i")));
                    refundQueryDetailDTO.setSettlementRefundFee(new Integer(response.get(key[3] + "i")));
                    refundQueryDetailDTO.setRefundStatus(response.get(key[4] + "i"));
                    refundQueryDetailDTO.setRefundRecvAccount(response.get(key[5] + "i"));
                    String refundTime = response.get(key[6] + "i");
                    Date date = StringUtil.formatDateValue(refundTime, RETURN_FORMATTER);
                    refundQueryDetailDTO.setRefundSuccessTime(date);
                    refundQueryDetailDTOS.add(refundQueryDetailDTO);
                    updateData.setRefundSuccessTime(date);
                }

                tradeRefundRecordMapper.updateRefundByInput(updateData);
            } else {
                refundQueryDTO.setResultCode(FAIL);
                refundQueryDTO.setErrCodeMsg("未获取到退款详细信息，请查看报文核实数据。");
            }

        } else {
            // 直接返回数据库查询结果
            refundQueryDTO.setResultCode(FAIL);
            if (RefundStatus.REFUND_SUCCESS.getCode().equals(refundRecordDO.getStatus())) {
                refundQueryDTO.setResultCode(SUCCESS);
                refundQueryDTO.setOriginalPlatOrderNo(refundRecordDO.getPlatOrderNo());
                refundQueryDTO.setOriginalOrderNo(refundRecordDO.getOrderNo());
                refundQueryDTO.setTotalFee(refundRecordDO.getOrderAmount());
                // TODO 用户实际支付金额是否是订单金额加上支付费率
                refundQueryDTO.setCashFee(refundRecordDO.getOrderAmount());

                RefundQueryDetailDTO refundQueryDetailDTO = new RefundQueryDetailDTO();
                refundQueryDetailDTO.setPlatRefundNo(refundRecordDO.getPlatRefundNo());
                refundQueryDetailDTO.setChannelRefundNo(refundRecordDO.getChannelRefundNo());
                refundQueryDetailDTO.setRefundFee(refundRecordDO.getSuccessRefundAmount());
                refundQueryDetailDTO.setSettlementRefundFee(refundRecordDO.getSuccessRefundAmount());
                refundQueryDetailDTO.setRefundStatus(SUCCESS);
                refundQueryDetailDTO.setRefundSuccessTime(refundRecordDO.getRefundSuccessTime());

                refundQueryDetailDTOS.add(refundQueryDetailDTO);
                refundQueryDTO.setRefundQueryDetailDTOS(refundQueryDetailDTOS);
            } else {
                refundQueryDTO.setErrCode(refundRecordDO.getErrCode());
                refundQueryDTO.setErrCodeMsg(refundQueryDTO.getErrCodeMsg());
            }

        }

        return new RefundQueryDTO();
    }

    /**
     * 作用：对账单下载（成功时返回对账单数据，失败时返回XML格式数据）<br>
     * 场景：付款码支付、公共号支付、扫码支付、APP支付
     *
     * @param wxDownloadBillReq 请求实体类数据
     * @return API返回数据
     * @throws Exception
     */
    @Override
    public BillDownloadDTO downloadBill(WxDownloadBillReq wxDownloadBillReq) throws BusinessException {

        String merchantNo = wxDownloadBillReq.getMerchantNo();

        String billDate = wxDownloadBillReq.getBillDate();
        // 校验日期格式
        if (!StringUtil.isValidDate(billDate, INPUT_FORMATTER)) {
            throw new BusinessException("您输入的日期格式不正确，请按照yyyyMMdd年月日格式输入");
        }

        // 将实体类对象转化成Map对象
        Map reqData = CommonUtil.getMapFromObject(wxDownloadBillReq, WxPayMethodType.downloadBill.getCode());

        JWellWXPayConfig jWellWXPayConfig = WxPayHandler.merchantInfoMap.get(merchantNo);
        if (jWellWXPayConfig == null) {
            throw new BusinessException("未获取到商户号为：" + merchantNo + "商户的相关配置信息，" +
                    "请联系相关工作人员进行商户数据确认或新增该商户" + merchantNo + "的配置信息。");
        }

        BillDownloadDTO billDownloadDTO = new BillDownloadDTO();

        // 判断该批次数据是否已经存在数据库中，如果存在则无需请求下载对账单接口
        String batchNo = wxDownloadBillReq.getBillDate();

        Map<String, Object> queryData = new HashMap<>(16);
        queryData.put("batchNo", batchNo);
        queryData.put("merchantNo", merchantNo);
        List<ChannelBillInfoDO> channelBillInfoDOS = channelBillInfoMapper.selectChannelBillInfo(queryData);

        if (channelBillInfoDOS != null && channelBillInfoDOS.size() > 0) {

            billDownloadDTO.setResultCode(SUCCESS);
            billDownloadDTO.setResultMsg("该【" + batchNo + "】日期的对账数据已经下载完成，无需重复操作，请到本地数据库中进行查看对账数据。");
            return billDownloadDTO;
        }

        reqData.put("bill_type", "ALL");
        Map<String, String> map = postAndReceiveData(jWellWXPayConfig, reqData, WxPayMethodType.downloadBill.getCode());

        if (!SUCCESS.equals(map.get("return_code"))) {

            billDownloadDTO.setResultCode(FAIL);
            billDownloadDTO.setErrCode(map.get("error_code"));
            billDownloadDTO.setErrCodeMsg(map.get("return_msg"));
            return billDownloadDTO;
        }

        // 如果存在数据的请求下获取data数据
        String data = map.get("data");
        String[] info = data.split("\\r\\n");
        List<ChannelBillInfoDO> billList = new ArrayList<>();

        // 至少需要4行，第一行标题抬头，第二行开始数据，倒数第二行统计抬头，倒数第一行统计数据
        if (info.length < 4) {
            billDownloadDTO.setResultCode(FAIL);
            billDownloadDTO.setResultMsg("对账数据格式异常，请查看返回数据的日志核对数据。");
            return billDownloadDTO;
        }

        for (int i = 1; i < info.length; i++) {

            String raw = info[i];
            if (raw.startsWith("`")) {
                raw = raw.substring(1);
            }

            logger.info("当前行数据：" + raw);
            String[] column = raw.split(",`");

            if (i < info.length - 2) {
                //﻿交易时间,            公众账号ID,             商户号,特约商户号,设备号,微信订单号,                商户订单号,      用户标识,                   交易类型,交易状态,付款银行,货币种类,应结订单金额,代金券金额,微信退款单号,商户退款单号,退款金额,充值券退款金额,退款类型,退款状态,商品名称,商户数据包,               手续费,费率, 订单金额,申请退款金额,费率备注
                //`2020-03-02 14:17:55,`wx8c4f683438292e27,`1562960801,`0,`,`4200000480202003022581846288,`2020030213120001,`o0bRy0_kzrcz3ug_eXNjp5VZrE6E,`JSAPI,`SUCCESS,`OTHERS,`CNY,     `0.01,      `0.00,  `0,             `0,     `0.00,  `0.00,          `,      `,  `自己来,`98880001::我自己来测试,`0.00000,`0.30%,`0.01,`0.00,         `
                ChannelBillInfoDO channelBillInfoDO = new ChannelBillInfoDO();
                // 将数据批量插入数据库，如果数据量过大，需要分批次处理
                channelBillInfoDO.setSysNo(globalSysnoGenerator.nextSysno());
                channelBillInfoDO.setMerchantNo(merchantNo);
                channelBillInfoDO.setBatchNo(batchNo);
                channelBillInfoDO.setChannelCode(PAY_TYPE);
                channelBillInfoDO.setPayTypeCode(column[8]);
                channelBillInfoDO.setChannelMerchantNo(column[2]);
                channelBillInfoDO.setPlatOrderNo(column[6]);
                channelBillInfoDO.setChannelOrderNo(column[5]);
                channelBillInfoDO.setTradeTime(StringUtil.getDateFromString(column[0], CHANNEL_BILL_FORMATTER));
                channelBillInfoDO.setPaySuccessTime(StringUtil.getDateFromString(column[0], CHANNEL_BILL_FORMATTER));
                channelBillInfoDO.setBuyerId(column[7]);

                channelBillInfoDO.setOrderAmount(new BigDecimal(column[24]));
                channelBillInfoDO.setTradeAmount(new BigDecimal(column[12]));
                channelBillInfoDO.setTradeAttach(column[21]);
                Integer status = 1;
                if (SUCCESS.equals(column[9])) {
                    status = 0;
                }

                channelBillInfoDO.setStatus(status);
                channelBillInfoDO.setPlatRefundOrderNo(column[15]);
                channelBillInfoDO.setChannelRefundOrderNo(column[14]);
                channelBillInfoDO.setRefundAmount(new BigDecimal(column[16]));
                if (SUCCESS.equals(column[19])) {
                    channelBillInfoDO.setRefundStatus(0);
                } else if (FAIL.equals(column[19])) {
                    channelBillInfoDO.setRefundStatus(1);
                }

                channelBillInfoDO.setChannelFee(new BigDecimal(column[22]));
                if (column[23].contains("%")) {
                    String[] feeRate = column[23].split("%");
                    BigDecimal feeRateData = new BigDecimal(feeRate[0]).divide(new BigDecimal(100));
                    channelBillInfoDO.setChannelFeeRate(feeRateData);
                }
                billList.add(channelBillInfoDO);

            } else if (i == info.length - 1) {

                List<WxBillTotalInfoDO> wxBillTotalInfoDOS = wxBillTotalInfoMapper.selectChannelBillInfo(queryData);

                // 没有导入数据则进行导入，如果已经导入了则不进行任何操作
                if (wxBillTotalInfoDOS.size() == 0) {
                    // 倒数第二行为统计抬头，最后一行为统计数据
                    // 总交易单数,应结订单总金额,退款总金额,充值券退款总金额,手续费总金额,订单总金额,申请退款总金额
                    //  `43,    `32274.00,      `0.00,  `0.00,          `96.86000, `32274.00,    `0.00, return_msg=ok, return_code=SUCCESS
                    WxBillTotalInfoDO wxBillTotalInfoDO = new WxBillTotalInfoDO();
                    wxBillTotalInfoDO.setSysNo(globalSysnoGenerator.nextSysno());
                    wxBillTotalInfoDO.setMerchantNo(merchantNo);
                    wxBillTotalInfoDO.setBatchNo(batchNo);
                    wxBillTotalInfoDO.setBillCount(Integer.parseInt(column[0]));
                    wxBillTotalInfoDO.setTotalSettlementAmount(new BigDecimal(column[1]));
                    wxBillTotalInfoDO.setTotalRefund(new BigDecimal(column[2]));
                    wxBillTotalInfoDO.setChargeCouponAmount(new BigDecimal(column[3]));
                    wxBillTotalInfoDO.setTotalPayFee(new BigDecimal(column[4]));
                    wxBillTotalInfoDO.setTotalOrderAmount(new BigDecimal(column[5]));
                    wxBillTotalInfoDO.setTotalApplyRefund(new BigDecimal(column[6]));

                    int insertSelective = wxBillTotalInfoMapper.insertSelective(wxBillTotalInfoDO);
                    if (insertSelective < 1) {
                        logger.error("微信订单统计数据存入微信统计总表失败，请手动处理数据。[" + wxBillTotalInfoDO.toString() + "]");
                    }
                }
            }
        }

        int i = channelBillInfoMapper.batchInsertChannelBillInfo(billList);
        logger.info("下载对账数据条数为：" + (info.length - 3) + "条,成功存入数据库的数据为：" + i + "条。");
        if (i != info.length - 3) {
            logger.error("下载对账数据条数为：" + (info.length - 3) + "条,然而成功存入数据库的数据为：" + i + "条，请核查数据。");
        }
        billDownloadDTO.setResultCode(SUCCESS);
        billDownloadDTO.setResultMsg("对账数据成功下载到本地数据库，请到数据库中进行查看。");

        return billDownloadDTO;
    }

    /**
     * 作用：授权码查询OPENID接口<br>
     * 场景：付款码支付
     *
     * @return API返回数据
     * @throws Exception
     */
    @Override
    public Map<String, String> authCodeToOpenid(String authCode, String merchantNo) throws BusinessException {

        JWellWXPayConfig jWellWXPayConfig = WxPayHandler.merchantInfoMap.get(merchantNo);
        if (jWellWXPayConfig == null) {
            throw new BusinessException("未获取到商户号为：" + merchantNo + "商户的相关配置信息，" +
                    "请联系相关工作人员进行商户数据确认或新增该商户" + merchantNo + "的配置信息。");
        }

        Map<String, String> map = new HashMap<>(16);
        map.put("auth_code", authCode);
        return postAndReceiveData(jWellWXPayConfig, map, WxPayMethodType.authCodeToOpenid.getCode());
    }

    private Map<String, String> postAndReceiveData(JWellWXPayConfig jWellWXPayConfig,
                                                   Map reqData, Integer methodType) throws BusinessException {

        WXPay wxPay = null;
        try {
            wxPay = new WXPay(jWellWXPayConfig);
        } catch (Exception e) {
            logger.error("微信支付初始化异常。");
        }

        Map<String, String> response = null;
        String reqTypeDesc = WxPayMethodType.getByCode(methodType).getMessage();

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
                response = null;
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

    private MerchantPayInfoDO findMerchantByMerchantNo(String merchantNo) throws BusinessException {

        // 校验
        Map<String, Object> data = new HashMap<>();
        // 正常状态
        data.put("status", 1);
        data.put("merchantNo", merchantNo);

        List<MerchantPayInfoDO> merchantPayInfoDOS = merchantPayInfoMapper.selectMerchantInfo(data);

        if (merchantPayInfoDOS == null || merchantPayInfoDOS.size() == 0) {
            throw new BusinessException("商户号对应商户不存在或者该商户已经被冻结（停用）");
        }

        return merchantPayInfoDOS.get(0);
    }

    private MerchantPayInfoDO findMerchantByChannelAndMerchantNo(String merchantNo,
                                                                 String payWayCode) throws BusinessException {

        //校验
        Map<String, Object> data = new HashMap<>();

        data.put("status", 1);
        data.put("merchantNo", merchantNo);
        data.put("payWayCode", payWayCode);
        List<MerchantPayInfoDO> merchantPayInfoDOS = merchantPayInfoMapper.selectMerchantInfo(data);
        if (merchantPayInfoDOS == null || merchantPayInfoDOS.size() == 0) {
            throw new BusinessException("商户号对应商户不存在");
        }

        return merchantPayInfoDOS.get(0);
    }

}
