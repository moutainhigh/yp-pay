package com.yp.pay.ali.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.internal.util.StringUtils;
import com.yp.pay.ali.config.AlipayConfiguration;
import com.yp.pay.ali.service.AliPayCallbackService;
import com.yp.pay.base.exception.BusinessException;
import com.yp.pay.common.enums.TradeStatus;
import com.yp.pay.common.util.StringUtil;
import com.yp.pay.entity.dto.AliCallBackInfoDTO;
import com.yp.pay.entity.dto.AliCallBackInfoDetailDTO;
import com.yp.pay.entity.dto.AliRefundCallBackInfoDTO;
import com.yp.pay.entity.entity.MerchantInfoDO;
import com.yp.pay.entity.entity.TradePaymentRecordDO;
import com.yp.pay.wx.mapper.MerchantInfoMapper;
import com.yp.pay.wx.mapper.TradePaymentRecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * @author: lijiang
 * @date: 2020.02.25 10:55
 * @description: AliPayCallbackServiceImpl
 */
@Service
public class AliPayCallbackServiceImpl implements AliPayCallbackService {

    @Autowired
    private AlipayConfiguration config;

    @Autowired
    private TradePaymentRecordMapper tradePaymentRecordMapper;

    @Autowired
    private MerchantInfoMapper merchantInfoMapper;

    private final static String ALI_TRADE_FINISHED = "TRADE_FINISHED";

    private final static String ALI_TRADE_SUCCESS = "TRADE_SUCCESS";

    private final static String ALI_TRADE_CLOSED = "TRADE_CLOSED";

    private static Logger log = LoggerFactory.getLogger(AliPayCallbackServiceImpl.class);

    /**
     * 第一步:验证签名,签名通过后进行第二步
     * 第二步：
     * 1、商户需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
     * 2、判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
     * 3、校验通知中的seller_id（或者seller_email)是否为out_trade_no这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email），
     * 4、验证app_id是否为该商户本身。上述1、2、3、4有任何一个验证不通过，则表明本次通知是异常通知，务必忽略。
     * 在上述验证通过后商户必须根据支付宝不同类型的业务通知，正确的进行不同的业务处理，并且过滤重复的通知结果数据。
     * 在支付宝的业务通知中，只有交易通知状态为TRADE_SUCCESS或TRADE_FINISHED时，支付宝才会认定为买家付款成功。
     *
     * @param params
     * @return
     * @throws BusinessException
     */
    @Override
    public AliCallBackInfoDTO dealAliPayCallBackData(Map<String, String> params) throws BusinessException {
        AliCallBackInfoDTO result = new AliCallBackInfoDTO();
        log.info("支付宝网站支付回调内容Map参数{}", params.toString());


        //**************************验签通过，开始校验是否为有效通知*********************
        String orderNo = params.get("out_trade_no");
        Example example = new Example(TradePaymentRecordDO.class);
        example.createCriteria().andEqualTo("merchantOrderNo", orderNo);
        TradePaymentRecordDO exist = tradePaymentRecordMapper.selectOneByExample(example);
        //2.校验订单号是否存在
        if (exist == null) {
            log.error("数据库不存在订单号为{}的记录，作为无效通知处理", orderNo);
            throw new BusinessException("数据库不存在订单号为" + orderNo + "的记录，作为无效通知处理");
        }
        //3.校验订单金额是否一致
        String amount = params.get("total_amount");
        if (!amount.equals(exist.getOrderAmount().toString())) {
            log.error("订单号为{}的记录总金额{}与通知金额{}不相符，作为无效通知处理", orderNo, exist.getOrderAmount(), amount);
            throw new BusinessException("数据库订单总金额" + exist.getOrderAmount() + "与通知金额" + amount + "不相符，作为无效通知处理");
        }
        Example merExample = new Example(MerchantInfoDO.class);
        merExample.createCriteria().andEqualTo("merchantNo", exist.getMerchantNo())
                .andEqualTo("payWayCode", "ALI_PAY");
        MerchantInfoDO merchant = merchantInfoMapper.selectOneByExample(merExample);
        //4.校验seller_id,默认为商户签约账号对应的支付宝用户ID
        String sellerId = params.get("seller_id");
        if (!sellerId.equals(merchant.getPartnerId())) {
            log.error("支付宝通知sellerId{}与商户支付配置pid{}不一致，处理为无效通知", sellerId, merchant.getPartnerId());
            throw new BusinessException("支付宝通知sellerId" + sellerId + "与商户支付配置pid" + merchant.getPartnerId() + "不一致，处理为无效通知");
        }
        //5.校验app_id
        String appId = params.get("app_id");
        if (!appId.equals(merchant.getAppId())) {
            log.error("支付宝通知appId{}与商户支付配置appId{}不一致，处理为无效通知", appId, merchant.getAppId());
            throw new BusinessException("支付宝通知appId" + appId + "与商户支付配置appId" + merchant.getAppId() + "不一致，处理为无效通知");
        }

        // 1.调用SDK验证签名
        try {
            boolean signVerified = AlipaySignature.rsaCheckV1(params, merchant.getAliPublicKey(),
                    config.getCharset(), config.getSignType());
            if (!signVerified) {
                log.error("支付宝验签未通过！请检查");
                throw new BusinessException("支付宝验签未通过");
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
            throw new BusinessException("支付宝验签异常");
        }


        //**************************校验结束，判定为有效通知，更新数据库状态*********************
        result.setDealSuccess(true);
        result.setUrl(merchant.getMerNotifyUrl());
        AliCallBackInfoDetailDTO callInfo = new AliCallBackInfoDetailDTO();

        //若订单状态已经为终态（成功或失败，则直接返回处理结果，不变更数据库）
        if (TradeStatus.SUCCESS.getCode().equals(exist.getStatus()) || TradeStatus.FAIL.getCode().equals(exist.getStatus())) {
            log.error("订单{}状态不为处理中，处理为无效通知，直接返回结果", exist.getOrderNo());
            if (TradeStatus.SUCCESS.getCode().equals(exist.getStatus())) {
                callInfo.setResultCode("SUCCESS");
            } else if (TradeStatus.FAIL.getCode().equals(exist.getStatus())) {
                callInfo.setResultCode("FAIL");
            }
            callInfo.setBuyerId(params.get("buyer_id"));
            callInfo.setBuyerLoginId(params.get("buyer_logon_id"));
            callInfo.setMerchantNo(merchant.getMerchantNo());
            callInfo.setOrderNo(exist.getOrderNo());
            callInfo.setTransactionId(exist.getChannelOrderNo());
            if (exist.getCreateDate() != null) {
                callInfo.setGmtCreate(StringUtil.formatDate(exist.getCreateDate(), "yyyy-MM-dd HH:mm:ss"));
            }
            if (exist.getPaySuccessTime() != null) {
                callInfo.setGmtPayment(StringUtil.formatDate(exist.getPaySuccessTime(), "yyyy-MM-dd HH:mm:ss"));
            }
            callInfo.setPayType("ALI_PAY");
            callInfo.setFeeType("CNY");
            callInfo.setTotalAmount(exist.getOrderAmount().toString());
            callInfo.setReceiptAmount(exist.getOrderAmount().toString());
            callInfo.setAttach(exist.getTradeAttach());
            result.setAliCallBackInfoDetailDTO(callInfo);
            return result;
        }

        String tradeStatus = params.get("trade_status");
        if (ALI_TRADE_SUCCESS.equals(tradeStatus) || ALI_TRADE_FINISHED.equals(tradeStatus)) {
            //交易成功
            callInfo.setResultCode("SUCCESS");
            exist.setStatus(TradeStatus.SUCCESS.getCode());
            exist.setChannelOrderNo(params.get("trade_no"));
            exist.setPaySuccessTime(StringUtil.parseDate(params.get("gmt_payment")));
            exist.setPaySuccessTime(new Date());
            exist.setRemark(params.get("buyer_id") + "-" + params.get("buyer_logon_id"));
            exist.setModifyUser("支付宝回调");
        } else if (ALI_TRADE_CLOSED.equals(tradeStatus)) {
            //交易失败
            callInfo.setResultCode("FAIL");
            exist.setStatus(TradeStatus.FAIL.getCode());
            exist.setChannelOrderNo(params.get("trade_no"));
            exist.setPaySuccessTime(new Date());
            exist.setRemark(params.get("buyer_id") + "-" + params.get("buyer_logon_id"));
            exist.setModifyUser("支付宝回调");
        } else {
            throw new BusinessException("未知通知状态：" + tradeStatus);
        }
        //另起线程，redis锁防止重复提交，修改本地数据库
        Callable<Integer> call = () -> {
            Example updateExample = new Example(TradePaymentRecordDO.class);
            updateExample.createCriteria().andEqualTo("sysno", exist.getSysNo())
                    .andEqualTo("version", exist.getVersion())
                    .andEqualTo("orderNo", exist.getOrderNo());
            exist.setVersion(exist.getVersion() + 1);
            int row = tradePaymentRecordMapper.updateByExampleSelective(exist, updateExample);
            if (row != 1) {
                log.error("网站支付回调更新数据库失败，订单号为" + exist.getOrderNo());
            }
            return 1;
        };
        FutureTask<Integer> task = new FutureTask<>(call);
        Thread thread = new Thread(task);
        thread.start();

        //*************************组装返回openapi通知处理结果*******************************
        callInfo.setBuyerId(params.get("buyer_id"));
        callInfo.setBuyerLoginId(params.get("buyer_logon_id"));
        callInfo.setMerchantNo(merchant.getMerchantNo());
        callInfo.setOrderNo(params.get("out_trade_no"));
        callInfo.setTransactionId(params.get("trade_no"));
        callInfo.setGmtCreate(params.get("gmt_create"));
        callInfo.setGmtPayment(params.get("gmt_payment"));
        callInfo.setGmtClose(params.get("gmt_close"));
        callInfo.setGmtRefund(params.get("gmt_refund"));
        callInfo.setPayType("ALI_PAY");
        callInfo.setFeeType("CNY");
        callInfo.setAttach(StringUtils.isEmpty(params.get("passback_params")) ? params.get("passback_params") : URLDecoder.decode(params.get("passback_params")));
        callInfo.setTotalAmount(params.get("total_amount"));
        callInfo.setReceiptAmount(params.get("receipt_amount"));
        result.setAliCallBackInfoDetailDTO(callInfo);
        return result;
    }

    @Override
    public AliRefundCallBackInfoDTO dealAliRefundBackData(Map<String, String> params) throws BusinessException {
        return null;
    }

    /**
     * 将request中的参数转换成Map
     */
    private Map<String, String> convertRequestParamsToMap(HttpServletRequest request) {
        Map<String, String> retMap = new HashMap<String, String>(16);

        Set<Map.Entry<String, String[]>> entrySet = request.getParameterMap().entrySet();

        for (Map.Entry<String, String[]> entry : entrySet) {
            String name = entry.getKey();
            String[] values = entry.getValue();
            int valLen = values.length;

            if (valLen == 1) {
                retMap.put(name, values[0]);
            } else if (valLen > 1) {
                StringBuilder sb = new StringBuilder();
                for (String val : values) {
                    sb.append(",").append(val);
                }
                retMap.put(name, sb.toString().substring(1));
            } else {
                retMap.put(name, "");
            }
        }

        return retMap;
    }

}
