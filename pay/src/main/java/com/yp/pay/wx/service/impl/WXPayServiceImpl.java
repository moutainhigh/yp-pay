package com.yp.pay.wx.service.impl;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.yp.pay.base.exception.BusinessException;
import com.yp.pay.common.enums.AliAndWXPayStatus;
import com.yp.pay.common.enums.TradeStatus;
import com.yp.pay.common.enums.WXPayMethodType;
import com.yp.pay.common.util.EntityConverter;
import com.yp.pay.common.util.GlobalSysnoGenerator;
import com.yp.pay.common.util.HttpClient;
import com.yp.pay.common.util.StringUtil;
import com.yp.pay.entity.aliandwx.dto.*;
import com.yp.pay.entity.aliandwx.req.*;
import com.yp.pay.wx.config.CommonUtil;
import com.yp.pay.wx.config.JWellWXPayConfig;
import com.yp.pay.wx.config.WXPayProperties;
import com.yp.pay.entity.aliandwx.dao.ChannelBillInfoDO;
import com.yp.pay.entity.aliandwx.dao.MerchantPayInfoDO;
import com.yp.pay.entity.aliandwx.dao.TradePaymentRecordDO;
import com.yp.pay.wx.handler.WXPayHandler;
import com.yp.pay.wx.mapper.ChannelBillInfoMapper;
import com.yp.pay.wx.mapper.MerchantPayInfoMapper;
import com.yp.pay.wx.mapper.TradePaymentRecordMapper;
import com.yp.pay.wx.service.WXPayService;
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

@Service
public class WXPayServiceImpl implements WXPayService {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TradePaymentRecordMapper tradePaymentRecordMapper;

    @Autowired
    private ChannelBillInfoMapper channelBillInfoMapper;

    @Autowired
    private GlobalSysnoGenerator globalSysnoGenerator;

    @Autowired
    private MerchantPayInfoMapper merchantPayInfoMapper;

    @Autowired
    private WXPayProperties properties;

    private static final String INPUT_FORMATTER = "yyyyMMdd";

    private static final String RETURN_FORMATTER = "yyyyMMddhhmmss";

    private static final String CHANNEL_BILL_FORMATTER = "yyyy-MM-dd HH:mm:ss";

    private final static String SUCCESS = "SUCCESS";

    private final static String FAIL = "FAIL";

    private final static String PAY_TYPE = "WX_PAY";

    private final static String PAY_NOTICE_URL = "/scanPayNotify";

    private final static String REFUND_NOTICE_URL = "/refundNotify";

    private static final String WX_JSAPI = "JSAPI";

    private final static BigDecimal PAY_FEE = new BigDecimal(0);

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
        tradePaymentRecordDO.setMerchantOrderNo(orderNo);
        // 判断该笔订单是否已经存在 防止重复支付
        List<TradePaymentRecordDO> tradePaymentExist = findTradePayment(tradePaymentRecordDO);
        if (tradePaymentExist == null || tradePaymentExist.size() == 0) {
            throw new BusinessException("未查询到到商户编号[" + merchantNo + "]下的支付订单[" + orderNo + "]，请核对请求数据。");
        }

        TradePaymentRecordDTO tradePaymentRecordDTO = EntityConverter.copyAndGetSingle(
                tradePaymentExist.get(0), TradePaymentRecordDTO.class);

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

        JWellWXPayConfig jWellWXPayConfig = WXPayHandler.merchantInfoMap.get(merchantNo);
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
    public ScanCodeDTO getQrCodeInfo(QrCodeInfoReq qrCodeInfoReq) throws BusinessException {

        String merchantNo = qrCodeInfoReq.getMerchantNo();

        // 通过商户号获取商户信息，下面只是获取了商户名字
        MerchantPayInfoDO merchant = findMerchantByMerchantNo(merchantNo);

        // 不同平台之前的订单号可以重复（因为不同平台的APPID、秘钥、微信分配的商户号以及证书都不同）
        String orderNo = qrCodeInfoReq.getOrderNo();
        String subject = qrCodeInfoReq.getSubject();
        String detail = qrCodeInfoReq.getDetail();
        String attach = qrCodeInfoReq.getAttach();
        String clientIp = qrCodeInfoReq.getClientIp();
        BigDecimal amount = qrCodeInfoReq.getAmount();


        TradePaymentRecordDO tradePaymentRecordDO = new TradePaymentRecordDO();
        tradePaymentRecordDO.setMerchantOrderNo(orderNo);
        // 判断该笔订单是否已经存在 防止重复支付
        List<TradePaymentRecordDO> tradePaymentExist = findTradePayment(tradePaymentRecordDO);

        if (tradePaymentExist != null && tradePaymentExist.size() > 0) {
            throw new BusinessException("支付请求订单号[" + orderNo + "]重复，请保证每笔订单号必须唯一。");
        }

        Long sysNo = globalSysnoGenerator.nextSysno();
        tradePaymentRecordDO.setSysno(sysNo);
        tradePaymentRecordDO.setMerchantNo(merchantNo);
        tradePaymentRecordDO.setMerchantName(merchant.getMerchantName());
        tradePaymentRecordDO.setVersion(1);
        tradePaymentRecordDO.setProductName(subject);
        tradePaymentRecordDO.setOrderIp(clientIp);
        tradePaymentRecordDO.setOrderAmount(amount);
        // TODO 费率暂时设置为0
        tradePaymentRecordDO.setFeeRate(PAY_FEE);
        tradePaymentRecordDO.setStatus(AliAndWXPayStatus.SUBMITTED.getCode());
        tradePaymentRecordDO.setCreateDate(new Date());
        tradePaymentRecordDO.setTradeDetail(detail);
        tradePaymentRecordDO.setTradeAttach(attach);
        tradePaymentRecordDO.setQrcodeStatus(0);
        tradePaymentRecordMapper.insertSelective(tradePaymentRecordDO);

        StringBuffer sb = new StringBuffer();
        sb.append("orderNo=" + orderNo);
        sb.append("&merchantNo=" + merchantNo);

        ScanCodeDTO scanCodeDTO = new ScanCodeDTO();
        scanCodeDTO.setOrderNo(orderNo);
        scanCodeDTO.setCodeUrl(properties.getQrCodeReturnUrl() + "?" + sb.toString());

        return scanCodeDTO;
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
    public String microPay(WXMicroPayReq microPayReq) throws BusinessException {

        String merchantNo = microPayReq.getMerchantNo();

        JWellWXPayConfig jWellWXPayConfig = WXPayHandler.merchantInfoMap.get(merchantNo);
        if (jWellWXPayConfig == null) {
            throw new BusinessException("未获取到商户号为：" + merchantNo + "商户的相关配置信息，" +
                    "请联系相关工作人员进行商户数据确认或新增该商户" + merchantNo + "的配置信息。");
        }

        TradePaymentRecordDO tradePaymentRecordDO = new TradePaymentRecordDO();

        // 不同平台之前的订单号可以重复（因为不同平台的APPID、秘钥、微信分配的商户号以及证书都不同）
        String orderNo = microPayReq.getOrderNo();
        tradePaymentRecordDO.setMerchantOrderNo(orderNo);

        // 判断该笔订单是否已经存在 防止重复支付
        List<TradePaymentRecordDO> tradePaymentExist = findTradePayment(tradePaymentRecordDO);
        if (tradePaymentExist != null && tradePaymentExist.size() > 0) {
            throw new BusinessException("支付请求订单号[" + orderNo + "]重复，请保证每笔订单号必须唯一。");
        }

        Long sysNo = globalSysnoGenerator.nextSysno();
        tradePaymentRecordDO.setSysno(sysNo);
        tradePaymentRecordDO.setMerchantNo(merchantNo);
        tradePaymentRecordDO.setMerchantName(jWellWXPayConfig.merchantPayInfoDO.getMerchantName());
        tradePaymentRecordDO.setProductName(microPayReq.getSubject());
        tradePaymentRecordDO.setVersion(1);
        tradePaymentRecordDO.setOrderIp(microPayReq.getClientIp());
        tradePaymentRecordDO.setOrderAmount(microPayReq.getAmount());
        // TODO 费率暂时设置为0
        tradePaymentRecordDO.setFeeRate(PAY_FEE);
        tradePaymentRecordDO.setPayWayCode(PAY_TYPE);
        tradePaymentRecordDO.setPayTypeCode("付款码支付");
        tradePaymentRecordDO.setStatus(AliAndWXPayStatus.SUBMITTED.getCode());
        tradePaymentRecordDO.setCreateDate(new Date());
        tradePaymentRecordDO.setTradeDetail(microPayReq.getDetail());
        tradePaymentRecordDO.setTradeAttach(microPayReq.getAttach());
        tradePaymentRecordMapper.insertSelective(tradePaymentRecordDO);

        // 将实体类对象转化成Map对象
        Map reqData = CommonUtil.getMapFromObject(microPayReq, WXPayMethodType.microPay.getCode());

        Map<String, String> response = postAndReceiveData(jWellWXPayConfig, reqData, WXPayMethodType.microPay.getCode());

        TradePaymentRecordDO updateData = new TradePaymentRecordDO();
        updateData.setSysno(sysNo);

        String return_code = response.get("return_code");
        String return_msg = response.get("return_msg");
        if (!SUCCESS.equals(return_code)) {
            updateData.setErrCode(return_code);
            updateData.setErrCodeDes(return_msg);
            tradePaymentRecordMapper.updateRecodeByInput(updateData);
            throw new BusinessException(return_msg);
        }

        String appid = response.get("appid");
        String mch_id = response.get("mch_id");
        String device_info = response.get("device_info");
        String nonce_str = response.get("nonce_str");
        String sign = response.get("sign");
        String result_code = response.get("result_code");
        String err_code = response.get("err_code");
        String err_code_des = response.get("err_code_des");
        if (!SUCCESS.equals(result_code)) {
            updateData.setErrCode(err_code);
            updateData.setErrCodeDes(err_code_des);

            tradePaymentRecordMapper.updateRecodeByInput(updateData);
            throw new BusinessException(err_code_des);
        }

        String openid = response.get("openid");//用户标识 用户在商户appid 下的唯一标识
        String is_subscribe = response.get("is_subscribe");//是否关注公众账号 Y-关注;N-未关注
        String trade_type = response.get("trade_type");//交易类型: MICROPAY 付款码支付
        String bank_type = response.get("bank_type");//付款银行
        String fee_type = response.get("fee_type");//货币类型 默认人民币：CNY
        String total_fee = response.get("total_fee");//订单金额
        String settlement_total_fee = response.get("settlement_total_fee");//应结订单金额:当订单使用了免充值型优惠券后返回该参数，应结订单金额=订单金额-免充值优惠券金额
        String coupon_fee = response.get("coupon_fee");//代金券金额:“代金券”金额<=订单金额，订单金额-“代金券”金额=现金支付金额，详见支付金额
        String cash_fee_type = response.get("cash_fee_type");//现金支付货币类型
        String cash_fee = response.get("cash_fee");//现金支付金额
        String transaction_id = response.get("transaction_id");//微信支付订单号
        String out_trade_no = response.get("out_trade_no");//商户订单号
        String time_end = response.get("time_end");//支付完成时间
        SimpleDateFormat sdf = new SimpleDateFormat(RETURN_FORMATTER);
        Date date = null;
        try {
            date = sdf.parse(time_end);
        } catch (ParseException e) {
            logger.error("日期格式转化异常，请核对银行返回报文中的支付完成时间格式。");
        }
        updateData.setCompleteTime(date);
        updateData.setStatus(AliAndWXPayStatus.SUCCESS.getCode());
        updateData.setChannelOrderNo(transaction_id);
        tradePaymentRecordMapper.updateRecodeByInput(updateData);
        return transaction_id;
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
    public ScanCodeDTO unifiedPay(WXUnifiedPayReq wxUnifiedPayReq) throws BusinessException {

        TradePaymentRecordDO updateData = new TradePaymentRecordDO();

//        String ipAddr = wxUnifiedPayReq.getClientIp();
//        String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\." +
//        "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."+
//        "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."+
//        "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
//        // 判断ip地址是否与正则表达式匹配
//        if (!ipAddr.matches(regex)){
//            throw new BusinessException("终端IP非法，请输入正确的终端IP地址。");
//        }

        String merchantNo = wxUnifiedPayReq.getMerchantNo();

        JWellWXPayConfig jWellWXPayConfig = WXPayHandler.merchantInfoMap.get(merchantNo);
        if (jWellWXPayConfig == null) {
            throw new BusinessException("未获取到商户号为：" + merchantNo + "商户的相关配置信息，" +
                    "请联系相关工作人员进行商户数据确认或新增该商户" + merchantNo + "的配置信息。");
        }

        ScanCodeDTO scanCodeDTO = new ScanCodeDTO();

        TradePaymentRecordDO tradePaymentRecordDO = new TradePaymentRecordDO();

        // 不同平台之前的订单号可以重复（因为不同平台的APPID、秘钥、微信分配的商户号以及证书都不同）
        String orderNo = wxUnifiedPayReq.getOrderNo();
        tradePaymentRecordDO.setMerchantOrderNo(orderNo);

        String tradeType = wxUnifiedPayReq.getTradeType();
        updateData.setPayTypeCode(tradeType);
        List<TradePaymentRecordDO> tradePaymentExist = findTradePayment(tradePaymentRecordDO);
        Long sysNo = globalSysnoGenerator.nextSysno();
        if (WX_JSAPI.equals(tradeType)) {
            // 如果是jspay,在扫码的时候已经将订单存入数据库
            if (tradePaymentExist == null || tradePaymentExist.size() == 0) {
                throw new BusinessException("支付请求订单号[" + orderNo + "]在订单表中并不存在，请核对请求订单信息。");
            }
            TradePaymentRecordDO paymentRecordDO = tradePaymentExist.get(0);
            sysNo = paymentRecordDO.getSysno();
            Integer status = paymentRecordDO.getStatus();
            // 满足数码仓，支付中的状态可以进行支付
//            if (!AliAndWXPayStatus.SUBMITTED.getCode().equals(status)) {
//                throw new BusinessException("订单号[" + orderNo + "]的订单已经提交支付，不能重复发起支付。");
//            }
            if (AliAndWXPayStatus.SUCCESS.getCode().equals(status)) {
                throw new BusinessException("订单号[" + orderNo + "]的订单已经支付成功，不能重复发起支付。");
            }
            if (AliAndWXPayStatus.FAIL.getCode().equals(status)) {
                throw new BusinessException("订单号[" + orderNo + "]的订单已经支付失败，不能重复发起支付。");
            }
            if (AliAndWXPayStatus.CLOSED.getCode().equals(status)) {
                throw new BusinessException("订单号[" + orderNo + "]的订单已经关闭，请重新下单生成二维码进行支付。");
            }
            Integer qrcodeStatus = paymentRecordDO.getQrcodeStatus();
            if (qrcodeStatus != null && qrcodeStatus == 3) {
                throw new BusinessException("该订单已被取消，不能支付，请重新生成二维码。");
            }
            updateData.setQrcodeStatus(1);
        } else {
            // 判断该笔订单是否已经存在 防止重复支付
            if (tradePaymentExist != null && tradePaymentExist.size() > 0) {
                throw new BusinessException("支付请求订单号[" + orderNo + "]重复，请保证每笔订单号必须唯一。");
            }
            tradePaymentRecordDO.setSysno(sysNo);
            tradePaymentRecordDO.setMerchantNo(merchantNo);
            tradePaymentRecordDO.setMerchantName(jWellWXPayConfig.merchantPayInfoDO.getMerchantName());
            tradePaymentRecordDO.setVersion(1);
            tradePaymentRecordDO.setProductName(wxUnifiedPayReq.getSubject());
            tradePaymentRecordDO.setOrderIp(wxUnifiedPayReq.getClientIp());
            tradePaymentRecordDO.setOrderAmount(wxUnifiedPayReq.getAmount());
            // TODO 费率暂时设置为0
            tradePaymentRecordDO.setFeeRate(PAY_FEE);
            tradePaymentRecordDO.setPayWayCode(PAY_TYPE);
            tradePaymentRecordDO.setPayTypeCode(tradeType);
            tradePaymentRecordDO.setStatus(AliAndWXPayStatus.SUBMITTED.getCode());
            tradePaymentRecordDO.setCreateDate(new Date());
            tradePaymentRecordDO.setTradeDetail(wxUnifiedPayReq.getDetail());
            tradePaymentRecordDO.setTradeAttach(wxUnifiedPayReq.getAttach());
            tradePaymentRecordMapper.insertSelective(tradePaymentRecordDO);
        }

        // 将实体类对象转化成Map对象
        Map reqData = CommonUtil.getMapFromObject(wxUnifiedPayReq, WXPayMethodType.scanPay.getCode());
        reqData.put("notify_url", jWellWXPayConfig.merchantPayInfoDO.getNotifyUrl() + PAY_NOTICE_URL);

        Map<String, String> response = postAndReceiveData(jWellWXPayConfig, reqData, WXPayMethodType.scanPay.getCode());

        updateData.setSysno(sysNo);
        updateData.setStatus(AliAndWXPayStatus.HANDING.getCode());//状态修改为处理中

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

        scanCodeDTO.setOrderNo(orderNo);
        scanCodeDTO.setPrepayId(prepay_id);
        scanCodeDTO.setCodeUrl(code_url);

        return scanCodeDTO;
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
    public TradePaymentRecordDTO orderQuery(WXOrderQueryOrReverseReq orderQueryOrReverseReq) throws BusinessException {

        // 将实体类对象转化成Map对象
        Map reqData = CommonUtil.getMapFromObject(orderQueryOrReverseReq, WXPayMethodType.orderQuery.getCode());

        TradePaymentRecordDO tradePaymentRecordDO = new TradePaymentRecordDO();

        String orderNo = orderQueryOrReverseReq.getOrderNo();
        if (StringUtils.isNotBlank(orderNo)) {
            tradePaymentRecordDO.setMerchantOrderNo(orderNo);
        }

        String channelOrderNo = orderQueryOrReverseReq.getChannelOrderNo();
        if (StringUtils.isNotBlank(channelOrderNo)) {
            tradePaymentRecordDO.setChannelOrderNo(channelOrderNo);
        }

        // 查询该笔订单是否存在
        List<TradePaymentRecordDO> tradePaymentExist = findTradePayment(tradePaymentRecordDO);
        if (tradePaymentExist == null || tradePaymentExist.size() == 0) {
            throw new BusinessException("数据库无法查询到该笔交易订单，请核实订单号是否输入正确或者输入是否匹配。");
        }

        TradePaymentRecordDO queryTradeRecordDo = tradePaymentExist.get(0);
        TradePaymentRecordDTO queryTradeRecord = EntityConverter.copyAndGetSingle(queryTradeRecordDo, TradePaymentRecordDTO.class);

        // 只有无订单状态、0：已提交、1：处理中和5：退款中的订单 需要到微信进行查询核实订单状态
        if (queryTradeRecord.getStatus() == null ||
                AliAndWXPayStatus.SUBMITTED.getCode() == queryTradeRecord.getStatus() ||
                AliAndWXPayStatus.HANDING.getCode() == queryTradeRecord.getStatus() ||
                AliAndWXPayStatus.REFUNDING.getCode() == queryTradeRecord.getStatus()) {

            String merchantNo = orderQueryOrReverseReq.getMerchantNo();

            JWellWXPayConfig jWellWXPayConfig = WXPayHandler.merchantInfoMap.get(merchantNo);
            if (jWellWXPayConfig == null) {
                throw new BusinessException("未获取到商户号为：" + merchantNo + "商户的相关配置信息，" +
                        "请联系相关工作人员进行商户数据确认或新增该商户" + merchantNo + "的配置信息。");
            }

            Map<String, String> response = postAndReceiveData(jWellWXPayConfig, reqData, WXPayMethodType.orderQuery.getCode());

            TradePaymentRecordDO updateData = new TradePaymentRecordDO();
            updateData.setSysno(queryTradeRecord.getSysno());

            String return_code = response.get("return_code");
            String return_msg = response.get("return_msg");
            if (!SUCCESS.equals(return_code)) {
                updateData.setErrCode(return_code);
                updateData.setErrCodeDes(return_msg);
                tradePaymentRecordMapper.updateRecodeByInput(updateData);
                throw new BusinessException(return_msg);
            }

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
                        updateData.setStatus(AliAndWXPayStatus.HANDING.getCode());
                    } else if ("CLOSED".equals(trade_state)) {
                        updateData.setStatus(AliAndWXPayStatus.CLOSED.getCode());
                    } else if ("REFUND".equals(trade_state)) {
                        // REFUND—转入退款 具体什么业务逻辑是转入退款 TODO
                        updateData.setStatus(AliAndWXPayStatus.REFUND.getCode());
                    } else if ("REVOKED".equals(trade_state)) {
                        //REVOKED—已撤销（付款码支付） TODO
                    } else {
                        updateData.setStatus(AliAndWXPayStatus.FAIL.getCode());
                        updateData.setErrCode(trade_state);
                    }
                    String detailDesc = "";
                    if (StringUtils.isNotBlank(trade_state_desc) || trade_state_desc.length() > 0) {
                        detailDesc = "," + trade_state_desc;
                    }
                    updateData.setErrCodeDes(WXPayHandler.payResultMap.get(trade_state) + detailDesc);
                    tradePaymentRecordMapper.updateRecodeByInput(updateData);
                    throw new BusinessException(err_code_des);
                }

                updateData.setStatus(AliAndWXPayStatus.SUCCESS.getCode());
                updateData.setChannelOrderNo(transaction_id);
                SimpleDateFormat sdf = new SimpleDateFormat(RETURN_FORMATTER);
                Date date = null;
                try {
                    date = sdf.parse(time_end);
                } catch (ParseException e) {
                    logger.error("日期格式转化异常，请核对银行返回报文中的支付完成时间格式。");
                }
                updateData.setCompleteTime(date);
                tradePaymentRecordMapper.updateRecodeByInput(updateData);

                queryTradeRecord.setStatus(AliAndWXPayStatus.SUCCESS.getCode());
                queryTradeRecord.setChannelOrderNo(transaction_id);
                queryTradeRecord.setCompleteTime(date);
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
    public CloseOrderDTO closeOrder(WXCloseOrderReq wxCloseOrderReq) throws BusinessException {
        // 判断请求参数是否为空
        String orderNo = wxCloseOrderReq.getOrderNo();

        TradePaymentRecordDO tradePaymentRecordDO = new TradePaymentRecordDO();
        tradePaymentRecordDO.setMerchantOrderNo(orderNo);

        // 查询该笔订单是否存在
        List<TradePaymentRecordDO> tradePaymentExist = findTradePayment(tradePaymentRecordDO);
        if (tradePaymentExist == null || tradePaymentExist.size() == 0) {
            throw new BusinessException("数据库无法查询到该笔交易订单，请核实订单号是否输入正确。");
        }

        TradePaymentRecordDO queryTradeRecordDo = tradePaymentExist.get(0);

        Long sysNo = queryTradeRecordDo.getSysno();

        CloseOrderDTO closeOrderDTO = new CloseOrderDTO();
        closeOrderDTO.setOrderNo(orderNo);
        closeOrderDTO.setResultCode(FAIL);

        TradePaymentRecordDO updateData = new TradePaymentRecordDO();
        updateData.setSysno(sysNo);

        //  只有无订单状态、0：已提交和1：处理中的订单可以进行关闭操作
        if ( AliAndWXPayStatus.HANDING.getCode() == queryTradeRecordDo.getStatus()) {

            // 将实体类对象转化成Map对象
            Map reqData = CommonUtil.getMapFromObject(wxCloseOrderReq, WXPayMethodType.closeOrder.getCode());

            String merchantNo = wxCloseOrderReq.getMerchantNo();

            JWellWXPayConfig jWellWXPayConfig = WXPayHandler.merchantInfoMap.get(merchantNo);
            if (jWellWXPayConfig == null) {
                throw new BusinessException("未获取到商户号为：" + merchantNo + "商户的相关配置信息，" +
                        "请联系相关工作人员进行商户数据确认或新增该商户" + merchantNo + "的配置信息。");
            }

            Map<String, String> response = postAndReceiveData(jWellWXPayConfig, reqData, WXPayMethodType.closeOrder.getCode());

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

            updateData.setStatus(AliAndWXPayStatus.CLOSED.getCode());//状态修改为处理中
            //将数据存入数据库
            tradePaymentRecordMapper.updateRecodeByInput(updateData);

        }if(AliAndWXPayStatus.SUBMITTED.getCode() == queryTradeRecordDo.getStatus()){
            updateData.setStatus(TradeStatus.PLAT_CLOSE.getCode());
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
    public Map<String, String> reverse(WXOrderQueryOrReverseReq orderQueryOrReverseReq) throws BusinessException {

        // 将实体类对象转化成Map对象
        Map reqData = CommonUtil.getMapFromObject(orderQueryOrReverseReq, WXPayMethodType.reverse.getCode());

        String merchantNo = orderQueryOrReverseReq.getMerchantNo();

        JWellWXPayConfig jWellWXPayConfig = WXPayHandler.merchantInfoMap.get(merchantNo);
        if (jWellWXPayConfig == null) {
            throw new BusinessException("未获取到商户号为：" + merchantNo + "商户的相关配置信息，" +
                    "请联系相关工作人员进行商户数据确认或新增该商户" + merchantNo + "的配置信息。");
        }

        return postAndReceiveData(jWellWXPayConfig, reqData, WXPayMethodType.reverse.getCode());
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
    public ApplyRefundDTO refund(WXRefundReq refundReq) throws BusinessException {

        // 将实体类对象转化成Map对象
        Map reqData = CommonUtil.getMapFromObject(refundReq, WXPayMethodType.refund.getCode());

        ApplyRefundDTO applyRefundDTO = new ApplyRefundDTO();

        // 判断当前退款的该笔订单是否存在
        TradePaymentRecordDO tradePaymentRecordDO = new TradePaymentRecordDO();

        String orderNo = refundReq.getOriginalOrderNo();
        if (StringUtils.isNotBlank(orderNo)) {
            tradePaymentRecordDO.setMerchantOrderNo(orderNo);
        }

        String channelOrderNo = refundReq.getOriginalChannelOrderNo();
        if (StringUtils.isNotBlank(channelOrderNo)) {
            tradePaymentRecordDO.setChannelOrderNo(channelOrderNo);
        }

        // 查询数据库该笔订单是否存在
        List<TradePaymentRecordDO> tradePaymentExist = findTradePayment(tradePaymentRecordDO);
        if (tradePaymentExist == null || tradePaymentExist.size() == 0) {
            throw new BusinessException("数据库无法查询到该笔交易订单，请核实订单号是否输入正确。");
        }

        TradePaymentRecordDO queryTradeRecordDo = tradePaymentExist.get(0);

        // 只有支付成功的订单才可以发起退款请求
        if (queryTradeRecordDo.getStatus() != null &&
                AliAndWXPayStatus.SUCCESS.getCode() == queryTradeRecordDo.getStatus()) {

            String merchantNo = refundReq.getMerchantNo();

            JWellWXPayConfig jWellWXPayConfig = WXPayHandler.merchantInfoMap.get(merchantNo);
            if (jWellWXPayConfig == null) {
                throw new BusinessException("未获取到商户号为：" + merchantNo + "商户的相关配置信息，" +
                        "请联系相关工作人员进行商户数据确认或新增该商户" + merchantNo + "的配置信息。");
            }

            reqData.put("notify_url", jWellWXPayConfig.merchantPayInfoDO.getNotifyUrl() + REFUND_NOTICE_URL);

            Map<String, String> response = postAndReceiveData(jWellWXPayConfig, reqData, WXPayMethodType.refund.getCode());

            TradePaymentRecordDO updateData = new TradePaymentRecordDO();
            updateData.setSysno(queryTradeRecordDo.getSysno());

            String return_code = response.get("return_code");
            String return_msg = response.get("return_msg");
            applyRefundDTO.setResultCode(return_code);
            if (!SUCCESS.equals(return_code)) {
                updateData.setErrCode(return_code);
                applyRefundDTO.setErrCode(return_code);
                updateData.setErrCodeDes(return_msg);
                applyRefundDTO.setErrCodeMsg(return_msg);
                tradePaymentRecordMapper.updateRecodeByInput(updateData);
                throw new BusinessException(return_msg);
            }

            // 业务结果 SUCCESS/FAIL
            String result_code = response.get("result_code");
            applyRefundDTO.setResultCode(result_code);
            String err_code = response.get("err_code");
            String err_code_des = response.get("err_code_des");

            updateData.setErrCode(err_code);
            applyRefundDTO.setErrCode(err_code);
            if (!SUCCESS.equals(result_code)) {
                updateData.setErrCodeDes(err_code_des);
                applyRefundDTO.setErrCodeMsg(err_code_des);
                tradePaymentRecordMapper.updateRecodeByInput(updateData);
                throw new BusinessException(err_code_des);
            }
            // 设置状态为退款中
            updateData.setStatus(AliAndWXPayStatus.REFUNDING.getCode());

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
            applyRefundDTO.setRefundFee(refund_fee);
            //订单金额
            String total_fee = response.get("total_fee");
            applyRefundDTO.setTotalFee(total_fee);
            //货币类型 默认人民币：CNY
            String fee_type = response.get("fee_type");
            applyRefundDTO.setFeeType(fee_type);
            //现金支付金额
            String cash_fee = response.get("cash_fee");
            applyRefundDTO.setCashFee(cash_fee);

            updateData.setRefundOrderNo(out_refund_no);
            updateData.setChannelRefundOrderNo(refund_id);
            tradePaymentRecordMapper.updateRecodeByInput(updateData);

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
    public RefundQueryDTO refundQuery(WXRefundQueryReq refundQueryReq) throws BusinessException {

        // 将实体类对象转化成Map对象
        Map reqData = CommonUtil.getMapFromObject(refundQueryReq, WXPayMethodType.refundQuery.getCode());

        String merchantNo = refundQueryReq.getMerchantNo();
        JWellWXPayConfig jWellWXPayConfig = WXPayHandler.merchantInfoMap.get(merchantNo);
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
        TradePaymentRecordDO tradePaymentRecordDO = new TradePaymentRecordDO();

        String originalOrderNo = refundQueryReq.getOriginalOrderNo();
        if (StringUtils.isNotBlank(originalOrderNo)) {
            tradePaymentRecordDO.setMerchantOrderNo(originalOrderNo);
        }

        String originalChannelOrderNo = refundQueryReq.getOriginalChannelOrderNo();
        if (StringUtils.isNotBlank(originalChannelOrderNo)) {
            tradePaymentRecordDO.setChannelOrderNo(originalChannelOrderNo);
        }

        String refundOrderNo = refundQueryReq.getRefundOrderNo();
        if (StringUtils.isNotBlank(refundOrderNo)) {
            tradePaymentRecordDO.setRefundOrderNo(refundOrderNo);
        }

        String channelRefundOrderNo = refundQueryReq.getChannelRefundOrderNo();
        if (StringUtils.isNotBlank(channelRefundOrderNo)) {
            tradePaymentRecordDO.setChannelRefundOrderNo(channelRefundOrderNo);
        }
        // 查询数据库该笔订单是否存在
        List<TradePaymentRecordDO> tradePaymentExist = findTradePayment(tradePaymentRecordDO);
        if (tradePaymentExist == null || tradePaymentExist.size() == 0) {
            throw new BusinessException("数据库无法查询到该笔交易订单，请核实订单号是否输入正确。");
        }

        TradePaymentRecordDO queryTradeRecordDo = tradePaymentExist.get(0);

        RefundQueryDTO refundQueryDTO = new RefundQueryDTO();
        List<RefundQueryDetailDTO> refundQueryDetailDTOS = new ArrayList<>();

        // 只有退款中和退款失败的订单查
        if (queryTradeRecordDo.getStatus() != null &&
                (AliAndWXPayStatus.REFUNDING.getCode() == queryTradeRecordDo.getStatus() ||
                        AliAndWXPayStatus.REFUND_ERR.getCode() == queryTradeRecordDo.getStatus())) {

            Map<String, String> response = postAndReceiveData(jWellWXPayConfig, reqData, WXPayMethodType.refundQuery.getCode());

            TradePaymentRecordDO updateData = new TradePaymentRecordDO();
            updateData.setSysno(queryTradeRecordDo.getSysno());

            String return_code = response.get("return_code");
            String return_msg = response.get("return_msg");
            refundQueryDTO.setResultCode(return_code);
            if (!SUCCESS.equals(return_code)) {
                updateData.setErrCode(return_code);
                refundQueryDTO.setErrCode(return_code);
                updateData.setErrCodeDes(return_msg);
                refundQueryDTO.setErrCodeMsg(return_msg);
                tradePaymentRecordMapper.updateRecodeByInput(updateData);
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
                refundQueryDTO.setErrCodeMsg(err_code_des);
                tradePaymentRecordMapper.updateRecodeByInput(updateData);
                throw new BusinessException(err_code_des);
            }
            // 设置状态为退款中
            updateData.setStatus(AliAndWXPayStatus.REFUND.getCode());

            //微信订单号
            String transaction_id = response.get("transaction_id");
            refundQueryDTO.setOriginalChannelOrderNo(transaction_id);
            //商户订单号
            String out_trade_no = response.get("out_trade_no");
            refundQueryDTO.setOriginalOrderNo(out_trade_no);
            //订单金额
            String total_fee = response.get("total_fee");
            refundQueryDTO.setTotalFee(new BigDecimal(total_fee));
            //现金支付金额
            String cash_fee = response.get("cash_fee");
            refundQueryDTO.setCashFee(new BigDecimal(cash_fee));
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
                    refundQueryDetailDTO.setRefundOrderNo(refundOrderNoReturn);
                    updateData.setRefundOrderNo(refundOrderNoReturn);

                    String channelRefundOrderNoReturn = response.get(key[1] + "i");
                    refundQueryDetailDTO.setChannelRefundOrderNo(channelRefundOrderNoReturn);
                    updateData.setChannelRefundOrderNo(channelRefundOrderNoReturn);

                    refundQueryDetailDTO.setRefundFee(new BigDecimal(response.get(key[2] + "i")));
                    refundQueryDetailDTO.setSettlementRefundFee(new BigDecimal(response.get(key[3] + "i")));
                    refundQueryDetailDTO.setRefundStatus(response.get(key[4] + "i"));
                    refundQueryDetailDTO.setRefundRecvAccount(response.get(key[5] + "i"));
                    String refundTime = response.get(key[6] + "i");
                    SimpleDateFormat sdf = new SimpleDateFormat(RETURN_FORMATTER);
                    Date date = null;
                    try {
                        date = sdf.parse(refundTime);
                    } catch (ParseException e) {
                        logger.error("日期格式转化异常，请核对银行返回报文中的支付完成时间格式。");
                    }
                    refundQueryDetailDTO.setRefundSuccessTime(date);
                    refundQueryDetailDTOS.add(refundQueryDetailDTO);
                    updateData.setRefundSuccessTime(date);
                }

                tradePaymentRecordMapper.updateRecodeByInput(updateData);
            } else {
                refundQueryDTO.setResultCode(FAIL);
                refundQueryDTO.setErrCodeMsg("未获取到退款详细信息，请查看报文核实数据。");
            }

        } else {
            // 直接放回数据库查询结果
            refundQueryDTO.setResultCode(FAIL);
            if (AliAndWXPayStatus.REFUND.getCode().equals(queryTradeRecordDo.getStatus())) {
                refundQueryDTO.setResultCode(SUCCESS);
                refundQueryDTO.setOriginalChannelOrderNo(queryTradeRecordDo.getChannelOrderNo());
                refundQueryDTO.setOriginalOrderNo(queryTradeRecordDo.getMerchantOrderNo());
                refundQueryDTO.setTotalFee(queryTradeRecordDo.getOrderAmount());
                // TODO 用户实际支付金额是否是订单金额加上支付费率
                refundQueryDTO.setCashFee(queryTradeRecordDo.getOrderAmount());

                RefundQueryDetailDTO refundQueryDetailDTO = new RefundQueryDetailDTO();
                refundQueryDetailDTO.setRefundOrderNo(queryTradeRecordDo.getRefundOrderNo());
                refundQueryDetailDTO.setChannelRefundOrderNo(queryTradeRecordDo.getChannelRefundOrderNo());
                refundQueryDetailDTO.setRefundFee(queryTradeRecordDo.getSuccessRefundAmount());
                refundQueryDetailDTO.setSettlementRefundFee(queryTradeRecordDo.getSuccessRefundAmount());
                refundQueryDetailDTO.setRefundStatus(SUCCESS);
                refundQueryDetailDTO.setRefundSuccessTime(queryTradeRecordDo.getRefundSuccessTime());

                refundQueryDetailDTOS.add(refundQueryDetailDTO);
                refundQueryDTO.setRefundQueryDetailDTOS(refundQueryDetailDTOS);
            } else {
                refundQueryDTO.setErrCode(queryTradeRecordDo.getErrCode());
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
    public BillDownloadDTO downloadBill(WXDownloadBillReq wxDownloadBillReq) throws BusinessException {

        String merchantNo = wxDownloadBillReq.getMerchantNo();

        String billDate = wxDownloadBillReq.getBillDate();
        // 校验日期格式
        if (!StringUtil.isValidDate(billDate, INPUT_FORMATTER)) {
            throw new BusinessException("您输入的日期格式不正确，请按照yyyyMMdd年月日格式输入");
        }

        // 将实体类对象转化成Map对象
        Map reqData = CommonUtil.getMapFromObject(wxDownloadBillReq, WXPayMethodType.downloadBill.getCode());

        JWellWXPayConfig jWellWXPayConfig = WXPayHandler.merchantInfoMap.get(merchantNo);
        if (jWellWXPayConfig == null) {
            throw new BusinessException("未获取到商户号为：" + merchantNo + "商户的相关配置信息，" +
                    "请联系相关工作人员进行商户数据确认或新增该商户" + merchantNo + "的配置信息。");
        }

        BillDownloadDTO billDownloadDTO = new BillDownloadDTO();

        // 判断该批次数据是否已经存在数据库中，如果存在则无需请求下载对账单接口
        String batchNo = wxDownloadBillReq.getBillDate();
        Map<String,Object> queryData = new HashMap<>();
        queryData.put("batchNo",batchNo);
        List<ChannelBillInfoDO> channelBillInfoDOS = channelBillInfoMapper.selectChannelBillInfo(queryData);
        if(channelBillInfoDOS!=null && channelBillInfoDOS.size()>0){
            billDownloadDTO.setResultCode(SUCCESS);
            billDownloadDTO.setResultMsg("该【"+batchNo+"】日期的对账数据已经下载完成，无需重复操作，请到本地数据库中进行查看对账数据。");
        }else{
            reqData.put("bill_type","ALL");
            Map<String, String> map = postAndReceiveData(jWellWXPayConfig, reqData, WXPayMethodType.downloadBill.getCode());
            if(!SUCCESS.equals(map.get("return_code"))){
                billDownloadDTO.setResultCode(FAIL);
                billDownloadDTO.setErrCode(map.get("error_code"));
                billDownloadDTO.setErrCodeMsg(map.get("return_msg"));
                return billDownloadDTO;
            }
            // 如果存在数据的请求下获取data数据
            String data = map.get("data");
            String[] info = data.split("\\r\\n");
            List<ChannelBillInfoDO> billList = new ArrayList<>();
            if(info.length<4){
                billDownloadDTO.setResultCode(FAIL);
                billDownloadDTO.setResultMsg("对账数据格式异常，请查看返回数据的日志核对数据。");
                return billDownloadDTO;
            }
            // TODO 最后两行为统计数据
            for (int i = 1; i < info.length-2; i++) {
                String raw = info[i];
                if(raw.startsWith("`")){
                    raw = raw.substring(1);
                }
                logger.info("当前行数据："+raw);
                String[] culumn = raw.split(",`");
                //﻿交易时间,            公众账号ID,             商户号,特约商户号,设备号,微信订单号,                商户订单号,      用户标识,                   交易类型,交易状态,付款银行,货币种类,应结订单金额,代金券金额,微信退款单号,商户退款单号,退款金额,充值券退款金额,退款类型,退款状态,商品名称,商户数据包,               手续费,费率, 订单金额,申请退款金额,费率备注
                //`2020-03-02 14:17:55,`wx8c4f683438292e27,`1562960801,`0,`,`4200000480202003022581846288,`2020030213120001,`o0bRy0_kzrcz3ug_eXNjp5VZrE6E,`JSAPI,`SUCCESS,`OTHERS,`CNY,     `0.01,      `0.00,  `0,             `0,     `0.00,  `0.00,          `,      `,  `自己来,`98880001::我自己来测试,`0.00000,`0.30%,`0.01,`0.00,         `
                ChannelBillInfoDO channelBillInfoDO = new ChannelBillInfoDO();
                // 将数据批量插入数据库，如果数据量过大，需要分批次处理
                channelBillInfoDO.setSysno(globalSysnoGenerator.nextSysno());
                channelBillInfoDO.setBatchNo(batchNo);
                channelBillInfoDO.setChannelCode(PAY_TYPE);
                channelBillInfoDO.setPayTypeCode(culumn[8]);
                channelBillInfoDO.setChannelMerchantNo(culumn[2]);
                channelBillInfoDO.setMerchantOrderNo(culumn[6]);
                channelBillInfoDO.setChannelOrderNo(culumn[5]);
                channelBillInfoDO.setTradeTime(StringUtil.getDateFromString(culumn[0],CHANNEL_BILL_FORMATTER));
                channelBillInfoDO.setPaySuccessTime(StringUtil.getDateFromString(culumn[0],CHANNEL_BILL_FORMATTER));
                channelBillInfoDO.setBuyerId(culumn[7]);
                channelBillInfoDO.setOrderAmount(new BigDecimal(culumn[24]));
                channelBillInfoDO.setTradeAmount(new BigDecimal(culumn[12]));
                channelBillInfoDO.setTradeAttach(culumn[21]);
                Integer status = 1;
                if(SUCCESS.equals(culumn[9])){
                    status = 0;
                }
                channelBillInfoDO.setStatus(status);
                channelBillInfoDO.setRefundOrderNo(culumn[15]);
                channelBillInfoDO.setChannelRefundOrderNo(culumn[14]);
                channelBillInfoDO.setRefundAmount(new BigDecimal(culumn[16]));
                if(SUCCESS.equals(culumn[19])){
                    channelBillInfoDO.setRefundStatus(0);
                }else if(FAIL.equals(culumn[19])){
                    channelBillInfoDO.setRefundStatus(1);
                }
                channelBillInfoDO.setChannelFee(new BigDecimal(culumn[22]));
                if(culumn[23].contains("%")){
                    String[] feeRate = culumn[23].split("%");
                    BigDecimal feeRateData = new BigDecimal(feeRate[0]).divide(new BigDecimal(100));
                    channelBillInfoDO.setChannelFeeRate(feeRateData);
                }
                billList.add(channelBillInfoDO);
            }
            int i = channelBillInfoMapper.batchInsertChannelBillInfo(billList);
            logger.info("下载对账数据条数为："+(info.length-3)+"条,成功存入数据库的数据为："+i+"条。");
            if(i!=info.length-3){
                logger.error("下载对账数据条数为："+(info.length-3)+"条,然而成功存入数据库的数据为："+i+"条，请核查数据。");
            }
            billDownloadDTO.setResultCode(SUCCESS);
            billDownloadDTO.setResultMsg("对账数据成功下载到本地数据库，请到数据库中进行查看。");
        }

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
    public Map<String, String> authCodeToOpenid(String merchantNo) throws BusinessException {

        JWellWXPayConfig jWellWXPayConfig = WXPayHandler.merchantInfoMap.get(merchantNo);
        if (jWellWXPayConfig == null) {
            throw new BusinessException("未获取到商户号为：" + merchantNo + "商户的相关配置信息，" +
                    "请联系相关工作人员进行商户数据确认或新增该商户" + merchantNo + "的配置信息。");
        }

        // TODO
        return postAndReceiveData(jWellWXPayConfig, new HashMap<>(), WXPayMethodType.authCodeToOpenid.getCode());
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
        String reqTypeDesc = WXPayMethodType.getByCode(methodType).getMessage();

        try {

            logger.info("请求微信接口数据：" + reqData.toString() + "，请求接口类型：" + reqTypeDesc);
            if (WXPayMethodType.microPay.getCode().equals(methodType)) {
                response = wxPay.microPay(reqData);

            } else if (WXPayMethodType.scanPay.getCode().equals(methodType)) {
                response = wxPay.unifiedOrder(reqData);

            } else if (WXPayMethodType.orderQuery.getCode().equals(methodType)) {
                response = wxPay.orderQuery(reqData);

            } else if (WXPayMethodType.closeOrder.getCode().equals(methodType)) {
                response = wxPay.closeOrder(reqData);

            } else if (WXPayMethodType.reverse.getCode().equals(methodType)) {
                response = wxPay.reverse(reqData);

            } else if (WXPayMethodType.refund.getCode().equals(methodType)) {
                response = wxPay.refund(reqData);

            } else if (WXPayMethodType.refundQuery.getCode().equals(methodType)) {
                response = wxPay.refundQuery(reqData);

            } else if (WXPayMethodType.downloadBill.getCode().equals(methodType)) {
                response = wxPay.downloadBill(reqData);

            } else if (WXPayMethodType.authCodeToOpenid.getCode().equals(methodType)) {
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

    private List<TradePaymentRecordDO> findTradePayment(TradePaymentRecordDO tradePaymentRecordDO) {

        List<TradePaymentRecordDO> tradePaymentExist = tradePaymentRecordMapper.select(tradePaymentRecordDO);

        return tradePaymentExist;
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
