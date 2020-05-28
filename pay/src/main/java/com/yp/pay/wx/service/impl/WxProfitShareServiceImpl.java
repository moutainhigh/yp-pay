package com.yp.pay.wx.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.yp.pay.base.exception.BusinessException;
import com.yp.pay.common.enums.WxProfitReceiverType;
import com.yp.pay.common.enums.WxRelationWithReceiver;
import com.yp.pay.common.util.HttpClient;
import com.yp.pay.entity.dto.*;
import com.yp.pay.entity.entity.TradePaymentRecordDO;
import com.yp.pay.entity.req.*;
import com.yp.pay.wx.config.JWellWXPayConfig;
import com.yp.pay.wx.handler.WxPayHandler;
import com.yp.pay.wx.mapper.TradePaymentRecordMapper;
import com.yp.pay.wx.service.WxProfitShareService;
import net.sf.json.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class WxProfitShareServiceImpl implements WxProfitShareService {

    Logger logger = LoggerFactory.getLogger(getClass());

    private final static String SUCCESS = "SUCCESS";

    private final static String FAIL = "FAIL";

    private final static String SIGN_TYPE = "HMAC-SHA256";

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

        // 存放业务参数
        Map<String, String> reqData = new HashMap<>(32);
        reqData.put("out_order_no", wxProfitShareSingleReq.getProfitShareNo());
        reqData.put("transaction_id", channelOrderNo);
        reqData.put("receivers", jsonArray.toString());

        // 组装参数，调用微信分账接口 TODO
        String url = "https://api.mch.weixin.qq.com/secapi/pay/profitsharing";

        // 将数据发送微信并接受返回数据封装到MAP集合中
        Map<String, String> response = postAndReceiveData(url, reqData, jWellWXPayConfig);

        // 如果分账成功 记录分账数据库中 TODO
        WxSingleProfitShareDTO wxSingleProfitShareDTO = new WxSingleProfitShareDTO();

        String return_code = response.get("return_code");
        String return_msg = response.get("return_msg");
        if (!SUCCESS.equals(return_code)) {
            wxSingleProfitShareDTO.setResultCode(FAIL);
            wxSingleProfitShareDTO.setErrCodeMsg(return_msg);
            throw new BusinessException(return_msg);
        }

        String result_code = response.get("result_code");
        String err_code = response.get("err_code");
        String err_code_des = response.get("err_code_des");
        if (!SUCCESS.equals(result_code)) {

            wxSingleProfitShareDTO.setResultCode(result_code);
            wxSingleProfitShareDTO.setErrCode(err_code);
            wxSingleProfitShareDTO.setErrCodeMsg(err_code_des);

            throw new BusinessException(err_code_des);
        }

        wxSingleProfitShareDTO.setResultCode(SUCCESS);
        wxSingleProfitShareDTO.setChannelOrderNo(response.get("transaction_id"));
        wxSingleProfitShareDTO.setProfitShareNo(response.get("out_order_no"));
        wxSingleProfitShareDTO.setChannelProfitShareNo(response.get("order_id"));


        return wxSingleProfitShareDTO;

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

        // 获取商户号，验证该商户是否在支付平台存在配置数据
        String merchantNo = wxProfitShareReceiverAddReq.getMerchantNo();

        JWellWXPayConfig jWellWXPayConfig = WxPayHandler.merchantInfoMap.get(merchantNo);
        if (jWellWXPayConfig == null) {
            throw new BusinessException("未获取到商户号为：" + merchantNo + "商户的相关配置信息，" +
                    "请联系相关工作人员进行商户数据确认或新增该商户" + merchantNo + "的配置信息。");
        }

        Integer type = wxProfitShareReceiverAddReq.getType();
        if (type == null) {
            throw new BusinessException("[分账接收方类型]参数不能为空。");
        }

        Integer relationType = wxProfitShareReceiverAddReq.getRelationType();
        if (relationType == null) {
            throw new BusinessException("[与分账方的关系类型]参数不能为空。");
        }

        JSONObject reqData = new JSONObject();
        reqData.put("type", WxProfitReceiverType.getByCode(type).getValue());
        reqData.put("account", wxProfitShareReceiverAddReq.getAccount());
        reqData.put("name", wxProfitShareReceiverAddReq.getName());
        reqData.put("relation_type", WxRelationWithReceiver.getByCode(relationType).getValue());
        reqData.put("custom_relation", wxProfitShareReceiverAddReq.getCustomRelation());

        Map<String, String> postData = new HashMap<>(16);
        postData.put("receiver",reqData.toJSONString());

        // 组装参数，调用微信分账接口 TODO
        String url = "https://api.mch.weixin.qq.com/pay/profitsharingaddreceiver";

        // 将数据发送微信并接受返回数据封装到MAP集合中
        Map<String, String> response = postAndReceiveData(url, postData, jWellWXPayConfig);

        // 如果添加分账用户成功则记录数据库中
        WxProfitShareReceiverAddDTO wxProfitShareReceiverAddDTO = new WxProfitShareReceiverAddDTO();

        String return_code = response.get("return_code");
        String return_msg = response.get("return_msg");
        if (!SUCCESS.equals(return_code)) {
            wxProfitShareReceiverAddDTO.setResultCode(FAIL);
            wxProfitShareReceiverAddDTO.setErrCodeMsg(return_msg);
            throw new BusinessException(return_msg);
        }

        String result_code = response.get("result_code");
        String err_code = response.get("err_code");
        String err_code_des = response.get("err_code_des");
        if (!SUCCESS.equals(result_code)) {

            wxProfitShareReceiverAddDTO.setResultCode(result_code);
            wxProfitShareReceiverAddDTO.setErrCode(err_code);
            wxProfitShareReceiverAddDTO.setErrCodeMsg(err_code_des);

            throw new BusinessException(err_code_des);
        }

        WxProfitShareReceiverSimpleDTO wxProfitShareReceiverSimpleDTO = new WxProfitShareReceiverSimpleDTO();
        wxProfitShareReceiverAddDTO.setResultCode(SUCCESS);
        String receiver = response.get("receiver");
        JSONObject receiverJson = JSONObject.parseObject(receiver);
        wxProfitShareReceiverSimpleDTO.setType(receiverJson.getString("type"));
        wxProfitShareReceiverSimpleDTO.setAccount(receiverJson.getString("account"));
        // 更新数据库
        wxProfitShareReceiverAddDTO.setWxProfitShareReceiverSimpleDTO(wxProfitShareReceiverSimpleDTO);


        return wxProfitShareReceiverAddDTO;
    }

    @Override
    public WxProfitShareReceiverAddDTO profitShareRemoveReceiver(
            WxProfitShareReceiverRemoveReq wxProfitShareReceiverRemoveReq) throws BusinessException {

        // 获取商户号，验证该商户是否在支付平台存在配置数据
        String merchantNo = wxProfitShareReceiverRemoveReq.getMerchantNo();

        JWellWXPayConfig jWellWXPayConfig = WxPayHandler.merchantInfoMap.get(merchantNo);
        if (jWellWXPayConfig == null) {
            throw new BusinessException("未获取到商户号为：" + merchantNo + "商户的相关配置信息，" +
                    "请联系相关工作人员进行商户数据确认或新增该商户" + merchantNo + "的配置信息。");
        }

        Integer type = wxProfitShareReceiverRemoveReq.getType();
        if (type == null) {
            throw new BusinessException("[分账接收方类型]参数不能为空。");
        }

        JSONObject reqData = new JSONObject();
        reqData.put("type", WxProfitReceiverType.getByCode(type).getValue());
        reqData.put("account", wxProfitShareReceiverRemoveReq.getAccount());

        Map<String, String> postData = new HashMap<>(16);
        postData.put("receiver",reqData.toJSONString());

        // 组装参数，调用微信分账接口 TODO
        String url = "https://api.mch.weixin.qq.com/pay/profitsharingremovereceiver";

        // 将数据发送微信并接受返回数据封装到MAP集合中
        Map<String, String> response = postAndReceiveData(url, postData, jWellWXPayConfig);

        // 如果添加分账用户成功则记录数据库中 将数据库数据进行删除操作
        WxProfitShareReceiverAddDTO wxProfitShareReceiverAddDTO = new WxProfitShareReceiverAddDTO();

        String return_code = response.get("return_code");
        String return_msg = response.get("return_msg");
        if (!SUCCESS.equals(return_code)) {
            wxProfitShareReceiverAddDTO.setResultCode(FAIL);
            wxProfitShareReceiverAddDTO.setErrCodeMsg(return_msg);
            throw new BusinessException(return_msg);
        }

        String result_code = response.get("result_code");
        String err_code = response.get("err_code");
        String err_code_des = response.get("err_code_des");
        if (!SUCCESS.equals(result_code)) {

            wxProfitShareReceiverAddDTO.setResultCode(result_code);
            wxProfitShareReceiverAddDTO.setErrCode(err_code);
            wxProfitShareReceiverAddDTO.setErrCodeMsg(err_code_des);

            throw new BusinessException(err_code_des);
        }

        WxProfitShareReceiverSimpleDTO wxProfitShareReceiverSimpleDTO = new WxProfitShareReceiverSimpleDTO();
        wxProfitShareReceiverAddDTO.setResultCode(SUCCESS);
        String receiver = response.get("receiver");
        JSONObject receiverJson = JSONObject.parseObject(receiver);
        wxProfitShareReceiverSimpleDTO.setType(receiverJson.getString("type"));
        wxProfitShareReceiverSimpleDTO.setAccount(receiverJson.getString("account"));
        // 更新数据库
        wxProfitShareReceiverAddDTO.setWxProfitShareReceiverSimpleDTO(wxProfitShareReceiverSimpleDTO);


        return wxProfitShareReceiverAddDTO;
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

    private Map<String, String> postAndReceiveData(String url, Map<String, String> postData, JWellWXPayConfig config) throws BusinessException {

        // 通用方法组装发送给微信的报文以及签名信息
        String xml = fillData(postData, config);
        logger.info("请求微信接口数据："+xml);

        try {

            xml = HttpClient.post(url, xml, "xml");
            logger.info("微信分账返回信息：" + xml);

            Map<String, String> response = processResponseXml(xml, config);
            return response;

        } catch (IOException e) {

            e.printStackTrace();
            throw new BusinessException("请求微信接口异常");
        } catch (Exception e) {

            e.printStackTrace();
            throw new BusinessException("接收微信接口数据后转Map时发生异常情况。");
        }
    }

    /**
     * @description: 对微信返回的XML数据进行验签，并将数据放入Map集合中
     * @author: liuX
     * @time: 2020/5/28 21:25
     * @params:
     * @return:
     */
    private Map<String, String> processResponseXml(String xmlStr, JWellWXPayConfig jWellWXPayConfig) throws Exception {

        String RETURN_CODE = "return_code";

        Map<String, String> respData = WXPayUtil.xmlToMap(xmlStr);

        if (respData.containsKey(RETURN_CODE)) {

            String return_code = respData.get(RETURN_CODE);
            if (return_code.equals(FAIL)) {

                return respData;
            } else if (return_code.equals(SUCCESS)) {

                // 验签
                if (WXPayUtil.isSignatureValid(respData, jWellWXPayConfig.getKey(), WXPayConstants.SignType.HMACSHA256)) {

                    return respData;
                } else {

                    throw new Exception(String.format("Invalid sign value in XML: %s", xmlStr));
                }

            } else {
                throw new Exception(String.format("return_code value %s is invalid in XML: %s", return_code, xmlStr));
            }

        } else {
            throw new Exception(String.format("No `return_code` in XML: %s", xmlStr));
        }
    }

    /**
     * @description: 将请求微信的MAP数据进行加密，并将数据拼装为微信需要的XML格式
     * @author: liuX
     * @time: 2020/5/28 21:22
     * @params: reqData 业务参数
     * @return: 拼装好请求微信的XML数据
     */
    private String fillData(Map<String, String> reqData, JWellWXPayConfig jWellWXPayConfig) throws BusinessException {

        // 存放公共参数
        String nonceStr = WXPayUtil.generateNonceStr();
        Map<String,String> map = new HashMap<>(16);
        map.put("appid", jWellWXPayConfig.getAppID());
        map.put("mch_id", jWellWXPayConfig.getMchID());
        map.put("nonce_str", nonceStr);
        map.put("sign_type", SIGN_TYPE);
        map.putAll(reqData);

        String sign;
        try {
            sign = WXPayUtil.generateSignature(map, jWellWXPayConfig.getKey(), WXPayConstants.SignType.HMACSHA256);
        } catch (Exception e) {
            throw new BusinessException("请求微信接口前对数据签名发生异常。");
        }

        StringBuffer postData = new StringBuffer();
        postData.append("<xml>");
        postData.append("<appid>");
        postData.append(jWellWXPayConfig.getAppID());
        postData.append("</appid>");
        postData.append("<mch_id>");
        postData.append(jWellWXPayConfig.getMchID());
        postData.append("</mch_id>");
        postData.append("<nonce_str>");
        postData.append(nonceStr);
        postData.append("</nonce_str>");
        Set<String> keySet = reqData.keySet();
        for (String key : keySet) {
            postData.append("<" + key + ">" + reqData.get(key) + "</" + key + ">");
        }
        postData.append("<sign>");
        postData.append(sign);
        postData.append("</sign>");
        postData.append("<sign_type>");
        postData.append(SIGN_TYPE);
        postData.append("</sign_type>");
        postData.append("</xml>");
        return postData.toString();
    }
}
