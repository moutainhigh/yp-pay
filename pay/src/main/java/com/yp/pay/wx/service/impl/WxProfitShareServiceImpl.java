package com.yp.pay.wx.service.impl;

import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.yp.pay.base.exception.BusinessException;
import com.yp.pay.common.util.HttpClient;
import com.yp.pay.entity.dto.WxProfitShareQueryDTO;
import com.yp.pay.entity.dto.WxProfitShareReceiverAddDTO;
import com.yp.pay.entity.dto.WxProfitShareReturnDTO;
import com.yp.pay.entity.dto.WxSingleProfitShareDTO;
import com.yp.pay.entity.entity.TradePaymentRecordDO;
import com.yp.pay.entity.req.*;
import com.yp.pay.wx.config.JWellWXPayConfig;
import com.yp.pay.wx.handler.WxPayHandler;
import com.yp.pay.wx.mapper.TradePaymentRecordMapper;
import com.yp.pay.wx.service.WxProfitShareService;
import net.sf.json.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WxProfitShareServiceImpl implements WxProfitShareService {

    @Autowired
    private TradePaymentRecordMapper tradePaymentRecordMapper;

    @Override
    public WxSingleProfitShareDTO singleProfitShare(WxProfitShareSingleReq wxProfitShareSingleReq) throws BusinessException {

        // 获取商户号，验证该商户是否在支付平台存在配置数据
        String merchantNo = wxProfitShareSingleReq.getMerchantNo();

        JWellWXPayConfig jWellWXPayConfig = WxPayHandler.merchantInfoMap.get(merchantNo);
        if (jWellWXPayConfig == null) {
            throw new BusinessException("未获取到商户号为：" + merchantNo + "商户的相关配置信息，" +
                    "请联系相关工作人员进行商户数据确认或新增该商户" + merchantNo + "的配置信息。");
        }

        // 验证订单号是否在平台存在支付数据
        String channelOrderNo = wxProfitShareSingleReq.getChannelOrderNo();
        if (StringUtils.isBlank(channelOrderNo)) {

            String orderNo = wxProfitShareSingleReq.getOrderNo();
            if (StringUtils.isNotBlank(orderNo)) {

                TradePaymentRecordDO tradePaymentRecordDO = tradePaymentRecordMapper.selectRecodeByChannelOrderNo(orderNo);
                if (tradePaymentRecordDO == null) {
                    throw new BusinessException("未查出到商户订单号[" + orderNo + "]的支付订单记录，请核实订单号是否输入正确。");
                }
                channelOrderNo = tradePaymentRecordDO.getChannelOrderNo();
            }

        } else {

            TradePaymentRecordDO tradePaymentRecordDO = tradePaymentRecordMapper.selectRecodeByChannelOrderNo(channelOrderNo);

            if (tradePaymentRecordDO == null) {
                throw new BusinessException("未查出到渠道订单号[" + channelOrderNo + "]的支付订单记录，请核实订单号是否输入正确。");
            }
        }

        // TODO 需要验证该笔订单是否已经发生分账（本地如果发生分账，但是商户却存在疑虑，需要调用渠进行查询，存在有误需要修复数据）
        // TODO 需要验证该笔分账单号是否已经存在
        List<WxProfitShareReceiverReq> wxProfitShareReceiverReqs = wxProfitShareSingleReq.getWxProfitShareReceiverReqs();
        JSONArray jsonArray = JSONArray.fromObject(wxProfitShareReceiverReqs);

        Map<String, String> reqData = new HashMap<>(32);
        reqData.put("appid", jWellWXPayConfig.getAppID());
        reqData.put("mch_id", jWellWXPayConfig.getMchID());
        reqData.put("nonce_str", WXPayUtil.generateNonceStr());
        reqData.put("sign_type", "HMAC-SHA256");
        reqData.put("out_order_no", wxProfitShareSingleReq.getProfitShareNo());
        reqData.put("transaction_id", wxProfitShareSingleReq.getChannelOrderNo());
        reqData.put("receivers", jsonArray.toString());

        try {
            String sign = WXPayUtil.generateSignature(reqData, jWellWXPayConfig.getKey(), WXPayConstants.SignType.HMACSHA256);
        } catch (Exception e) {
            throw new BusinessException("请求微信接口前对数据签名发生异常。");
        }

        // 组装参数，调用微信分账接口 TODO
        String url = "https://api.mch.weixin.qq.com/secapi/pay/profitsharing";
        StringBuffer postData = new StringBuffer();
        postData.append("<xml>");
        postData.append("<appid>");
        postData.append(jWellWXPayConfig.getAppID());
        postData.append("</appid>");
        postData.append("<mch_id>");
        postData.append(jWellWXPayConfig.getMchID());
        postData.append("</mch_id>");
        postData.append("<nonce_str>");
        postData.append(WXPayUtil.generateNonceStr());
        postData.append("</nonce_str>");
        postData.append("<out_order_no>");
        postData.append(wxProfitShareSingleReq.getProfitShareNo());
        postData.append("</out_order_no>");
        postData.append("<transaction_id>");
        postData.append(wxProfitShareSingleReq.getChannelOrderNo());
        postData.append("</transaction_id>");
        postData.append("<sign>");

        postData.append("</sign>");
        postData.append("<sign_type>");
        postData.append("HMAC-SHA256");
        postData.append("</sign_type>");
        postData.append("<receivers>");

        postData.append("</receivers>");
        postData.append("</xml>");
        try {
            HttpClient.post(url, postData.toString(), "xml");
        } catch (IOException e) {
            e.printStackTrace();
            throw new BusinessException("请求微信接口异常");
        }

        return null;
    }

    public static void main(String[] args) {
        List<WxProfitShareReceiverReq> wxProfitShareReceiverReqs = new ArrayList<>(16);
        for (int i = 0; i < 5; i++) {
            WxProfitShareReceiverReq receiverReqs = new WxProfitShareReceiverReq();
            receiverReqs.setAccount("zhangsan" + i);
            receiverReqs.setAmount(i);
            receiverReqs.setDescription("描述" + i);
            receiverReqs.setType("类型" + i);
            wxProfitShareReceiverReqs.add(receiverReqs);

        }
        JSONArray jsonArray = JSONArray.fromObject(wxProfitShareReceiverReqs);
        System.out.println(jsonArray.toString());

        Map<String, String> reqData = new HashMap<>(32);
        reqData.put("appid", "jWellWXPayConfig.getAppID()");
        reqData.put("mch_id", "jWellWXPayConfig.getMchID()");
        reqData.put("nonce_str", WXPayUtil.generateNonceStr());
        reqData.put("sign_type", "HMAC-SHA256");
        reqData.put("out_order_no", "wxProfitShareSingleReq.getProfitShareNo()");
        reqData.put("transaction_id", "wxProfitShareSingleReq.getChannelOrderNo()");
        reqData.put("receivers", jsonArray.toString());
        System.out.println(reqData.toString());
        try {
            String sign = WXPayUtil.generateSignature(reqData, "fV6ikwvuyv1MOc5n6zojvWIQkE7uZ08X", WXPayConstants.SignType.HMACSHA256);
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    @Override
    public WxSingleProfitShareDTO multiProfitShare(WxProfitShareSingleReq wxProfitShareSingleReq) throws BusinessException {
        return null;
    }

    @Override
    public WxProfitShareQueryDTO profitShareQuery(WxProfitShareQueryReq wxProfitShareQueryReq) throws BusinessException {
        return null;
    }

    @Override
    public WxProfitShareReceiverAddDTO profitShareAddReceiver(WxProfitShareReceiverAddReq wxProfitShareReceiverAddReq) throws BusinessException {
        return null;
    }

    @Override
    public WxProfitShareReceiverAddDTO profitShareRemoveReceiver(WxProfitShareReceiverSimpleReq wxProfitShareReceiverSimpleReq) throws BusinessException {
        return null;
    }

    @Override
    public WxSingleProfitShareDTO profitShareFinish(WxProfitShareFinishReq wxProfitShareFinishReq) throws BusinessException {
        return null;
    }

    @Override
    public WxProfitShareReturnDTO profitShareReturn(WxProfitShareReturnReq wxProfitShareReturnReq) throws BusinessException {
        return null;
    }

    @Override
    public WxProfitShareReturnDTO profitShareReturnQuery(WxProfitShareReturnQueryReq wxProfitShareReturnQueryReq) throws BusinessException {
        return null;
    }
}
