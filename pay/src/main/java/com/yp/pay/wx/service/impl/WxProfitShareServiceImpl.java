package com.yp.pay.wx.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.wxpay.sdk.WXPay;
import com.yp.pay.base.exception.BusinessException;
import com.yp.pay.common.enums.WxProfitReceiverType;
import com.yp.pay.common.enums.WxRelationWithReceiver;
import com.yp.pay.common.util.EntityConverter;
import com.yp.pay.common.util.GlobalSysnoGenerator;
import com.yp.pay.entity.dto.*;
import com.yp.pay.entity.entity.ProfitShareReceiverDO;
import com.yp.pay.entity.entity.TradePaymentRecordDO;
import com.yp.pay.entity.req.*;
import com.yp.pay.wx.config.JWellWXPayConfig;
import com.yp.pay.wx.handler.WxPayHandler;
import com.yp.pay.wx.mapper.ProfitShareReceiverMapper;
import com.yp.pay.wx.mapper.TradePaymentRecordMapper;
import com.yp.pay.wx.service.WxProfitShareService;
import net.sf.json.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WxProfitShareServiceImpl implements WxProfitShareService {

    Logger logger = LoggerFactory.getLogger(getClass());

    private final static String SUCCESS = "SUCCESS";

    private final static String FAIL = "FAIL";

    private final static String SINGLE_PROFIT_URL = "/secapi/pay/profitsharing";

    private final static String MULTI_PROFIT_URL = "/secapi/pay/multiprofitsharing";

    private final static String PROFIT_QUERY_URL = "/pay/profitsharingquery";

    private final static String PROFIT_RECEIVER_ADD_URL = "/pay/profitsharingaddreceiver";

    private final static String PROFIT_RECEIVER_REMOVE_URL = "/pay/profitsharingremovereceiver";

    private final static String PROFIT_FINISH_URL = "/secapi/pay/profitsharingfinish";

    private final static String PRIFIT_RETURN_URL = "/secapi/pay/profitsharingreturn";

    private final static String PRIFIT_RETURN_QUERY_URL = "/pay/profitsharingreturnquery";

    private final static String PAY_TYPE = "WX_PAY";

    @Autowired
    private GlobalSysnoGenerator globalSysnoGenerator;

    @Autowired
    private TradePaymentRecordMapper tradePaymentRecordMapper;

    @Autowired
    private ProfitShareReceiverMapper profitShareReceiverMapper;

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
        String orderNo = wxProfitShareSingleReq.getOrderNo();
        String channelOrderNo;
        if (StringUtils.isNotBlank(orderNo)) {

            TradePaymentRecordDO tradePaymentRecordDO = tradePaymentRecordMapper.selectRecodeByMerchantOrderNo(orderNo);
            if (tradePaymentRecordDO == null) {
                throw new BusinessException("未查出到商户支付订单号[" + orderNo + "]的支付订单记录，请核实订单号是否输入正确。");
            }
            channelOrderNo = tradePaymentRecordDO.getChannelOrderNo();

        } else {

            String platOrderNo = wxProfitShareSingleReq.getPlatOrderNo();
            TradePaymentRecordDO tradePaymentRecordDO = tradePaymentRecordMapper.selectRecodeByPlatOrderNo(platOrderNo);

            if (tradePaymentRecordDO == null) {
                throw new BusinessException("未查出到平台支付订单号[" + platOrderNo + "]的支付订单记录，请核实订单号是否输入正确。");
            }
            channelOrderNo = tradePaymentRecordDO.getChannelOrderNo();
        }

        // TODO 需要验证该笔订单是否已经发生分账（本地如果发生分账，但是商户却存在疑虑，需要调用渠进行查询，存在有误需要修复数据）
        // TODO 需要验证该笔分账单号是否已经存在
        List<WxProfitShareReceiverReq> wxProfitShareReceiverReqs = wxProfitShareSingleReq.getWxProfitShareReceiverReqs();
        List<WxProfitShareToWxReq> wxProfitShareToWxReqs = new ArrayList<>(10);
        for (WxProfitShareReceiverReq wxProfitShareReceiverReq : wxProfitShareReceiverReqs) {

            WxProfitShareToWxReq wxProfitShareToWxReq = EntityConverter.copyAndGetSingle(
                    wxProfitShareReceiverReq, WxProfitShareToWxReq.class);
            wxProfitShareToWxReq.setType(WxProfitReceiverType.getByCode(wxProfitShareReceiverReq.getReceiverType()).getValue());

            wxProfitShareToWxReqs.add(wxProfitShareToWxReq);
        }
        JSONArray jsonArray = JSONArray.fromObject(wxProfitShareToWxReqs);

        // 存放业务参数
        Map<String, String> reqData = new HashMap<>(32);
        reqData.put("out_order_no", wxProfitShareSingleReq.getProfitShareNo());
        reqData.put("transaction_id", channelOrderNo);

        reqData.put("receivers", jsonArray.toString());

        // 将数据发送微信并接受返回数据封装到MAP集合中
        Map<String, String> response = postAndReceiveData(SINGLE_PROFIT_URL, true, reqData, jWellWXPayConfig);
        logger.info("微信单次分账返回信息：" + response);

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

        Integer type = wxProfitShareReceiverAddReq.getReceiverType();
        if (type == null) {
            throw new BusinessException("[分账接收方类型]参数不能为空。");
        }

        Integer relationType = wxProfitShareReceiverAddReq.getRelationType();
        if (relationType == null) {
            throw new BusinessException("[与分账方的关系类型]参数不能为空。");
        }

        JSONObject reqData = new JSONObject();
        reqData.put("type", WxProfitReceiverType.getByCode(type).getValue());
        reqData.put("account", wxProfitShareReceiverAddReq.getReceiverAccount());
        reqData.put("name", wxProfitShareReceiverAddReq.getReceiverName());
        reqData.put("relation_type", WxRelationWithReceiver.getByCode(relationType).getValue());
        reqData.put("custom_relation", wxProfitShareReceiverAddReq.getCustomRelation());

        Map<String, String> postData = new HashMap<>(16);
        postData.put("receiver", reqData.toJSONString());

        // 将数据发送微信并接受返回数据封装到MAP集合中
        Map<String, String> response = postAndReceiveData(PROFIT_RECEIVER_ADD_URL, false, postData, jWellWXPayConfig);
        logger.info("微信添加分账接收方返回信息：" + response);

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

        ProfitShareReceiverDO profitShareReceiverDO = EntityConverter.copyAndGetSingle(
                wxProfitShareReceiverAddReq, ProfitShareReceiverDO.class);
        ProfitShareReceiverDO findReceiver = profitShareReceiverMapper.selectReceiverByEntity(profitShareReceiverDO);
        // 数据库中已经存在数据
        if (findReceiver != null) {
            // 状态为空或者为已删除状态，则更新状态
            if (findReceiver.getStatus() == null || findReceiver.getStatus().equals(2)) {
                findReceiver.setStatus(1);
                int i = profitShareReceiverMapper.updateByPrimaryKeySelective(findReceiver);
                if (i < 1) {
                    logger.error("添加分账接收方时，更新分账接收方状态到数据库中失败，请手动处理[" + findReceiver.toString() + "]");
                }
            }
        } else {
            profitShareReceiverDO.setSysNo(globalSysnoGenerator.nextSysno());
            profitShareReceiverDO.setStatus(1);
            profitShareReceiverDO.setPayWayCode(PAY_TYPE);
            profitShareReceiverDO.setCreateDate(new Date());
            int i = profitShareReceiverMapper.insert(profitShareReceiverDO);
            if (i < 1) {
                logger.error("添加分账接收方到数据库中失败，请手动处理[" + profitShareReceiverDO.toString() + "]");
            }
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

        Integer type = wxProfitShareReceiverRemoveReq.getReceiverType();
        if (type == null) {
            throw new BusinessException("[分账接收方类型]参数不能为空。");
        }

        JSONObject reqData = new JSONObject();
        reqData.put("type", WxProfitReceiverType.getByCode(type).getValue());
        reqData.put("account", wxProfitShareReceiverRemoveReq.getReceiverAccount());

        Map<String, String> postData = new HashMap<>(16);
        postData.put("receiver", reqData.toJSONString());

        // 将数据发送微信并接受返回数据封装到MAP集合中
        Map<String, String> response = postAndReceiveData(PROFIT_RECEIVER_REMOVE_URL, false, postData, jWellWXPayConfig);
        logger.info("微信删除分账接收方返回信息：" + response);

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

        // 删除成功，更新数据库数据状态
        ProfitShareReceiverDO profitShareReceiverDO = EntityConverter.copyAndGetSingle(
                wxProfitShareReceiverRemoveReq, ProfitShareReceiverDO.class);
        ProfitShareReceiverDO findReceiver = profitShareReceiverMapper.selectReceiverByEntity(profitShareReceiverDO);
        if(!findReceiver.getStatus().equals(2)){
            findReceiver.setStatus(2);
            int i = profitShareReceiverMapper.updateByPrimaryKeySelective(findReceiver);
            if (i < 1) {
                logger.error("删除分账接收方时，更新数据到数据库库中失败，请手动处理[" + findReceiver.toString() + "]");
            }
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

    private Map<String, String> postAndReceiveData(String url, Boolean withCert, Map<String, String> postData, JWellWXPayConfig config) throws BusinessException {

        WXPay wxPay = null;
        try {
            wxPay = new WXPay(config);
        } catch (Exception e) {
            logger.error("微信支付初始化异常。");
        }

        Map<String, String> fillMap;
        try {
            fillMap = wxPay.fillRequestData(postData);
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
                Map<String, String> responseMap = wxPay.processResponseXml(response);
                return responseMap;

            } else {
                throw new BusinessException("微信返回数据异常，返回信息为空");
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("提交微信请求时，发生异常。");
        }
    }

}
