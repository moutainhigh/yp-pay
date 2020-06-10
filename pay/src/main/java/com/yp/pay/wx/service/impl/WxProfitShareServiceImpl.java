package com.yp.pay.wx.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.yp.pay.base.exception.BusinessException;
import com.yp.pay.common.enums.*;
import com.yp.pay.common.util.EntityConverter;
import com.yp.pay.common.util.GlobalSysnoGenerator;
import com.yp.pay.common.util.StringUtil;
import com.yp.pay.entity.dto.*;
import com.yp.pay.entity.entity.ProfitShareDetailDO;
import com.yp.pay.entity.entity.ProfitShareReceiverDO;
import com.yp.pay.entity.entity.ProfitShareRecordDO;
import com.yp.pay.entity.entity.TradePaymentRecordDO;
import com.yp.pay.entity.req.*;
import com.yp.pay.wx.config.JWellWXPayConfig;
import com.yp.pay.wx.handler.WxPayHandler;
import com.yp.pay.wx.mapper.ProfitShareDetailMapper;
import com.yp.pay.wx.mapper.ProfitShareReceiverMapper;
import com.yp.pay.wx.mapper.ProfitShareRecordMapper;
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

    private final static String PREFIX_SHARE = "SHARE";

    private static final String PLAT_ORDER_PART = "yyyyMMddhhmmss";

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

    @Autowired
    private ProfitShareRecordMapper profitShareRecordMapper;

    @Autowired
    private ProfitShareDetailMapper profitShareDetailMapper;

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
        String platOrderNo;
        String channelOrderNo;
        TradePaymentRecordDO tradePaymentRecordDO;
        if (StringUtils.isNotBlank(orderNo)) {

            tradePaymentRecordDO = tradePaymentRecordMapper.selectRecodeByOrderNo(orderNo);
            if (tradePaymentRecordDO == null) {
                throw new BusinessException("未查出到商户支付订单号[" + orderNo + "]的支付订单记录，请核实订单号是否输入正确。");
            }
            platOrderNo = tradePaymentRecordDO.getPlatOrderNo();
            channelOrderNo = tradePaymentRecordDO.getChannelOrderNo();

        } else {

            platOrderNo = wxProfitShareSingleReq.getPlatOrderNo();
            tradePaymentRecordDO = tradePaymentRecordMapper.selectRecodeByPlatOrderNo(platOrderNo);

            if (tradePaymentRecordDO == null) {
                throw new BusinessException("未查出到平台支付订单号[" + platOrderNo + "]的支付订单记录，请核实订单号是否输入正确。");
            }
            orderNo = tradePaymentRecordDO.getOrderNo();
            channelOrderNo = tradePaymentRecordDO.getChannelOrderNo();
        }

        // 非分账支付不能进行分账操作
        Integer profitShareSign = tradePaymentRecordDO.getProfitShareSign();
        if(ProfitShareSign.UN_SHARE.getCode().equals(profitShareSign)){
            throw new BusinessException("该笔订单号[" + platOrderNo + "]的支付订单不支持分账。");
        }

        // 分账完成的订单和已经分账回退的订单无法进行分账
        Integer profitShareStatus = tradePaymentRecordDO.getProfitShareStatus();
        if(ProfitShareStatus.SHARE_DONE.getCode().equals(profitShareStatus)){
            throw new BusinessException("该笔订单号[" + platOrderNo + "]的支付订单已完成分账，请勿重复分账。");
        }
        if(ProfitShareStatus.SHARE_REFUND.getCode().equals(profitShareStatus)){
            throw new BusinessException("该笔订单号[" + platOrderNo + "]的支付订单已完成分账回退，请勿再次进行分账。");
        }

        String profitShareNo = wxProfitShareSingleReq.getProfitShareNo();
        ProfitShareRecordDO profitShareRecordDO = profitShareRecordMapper.selectRecodeByProfitShareNo(profitShareNo);
        // 如果当前分账单号的记录在数据库中存在，则提示用户修改分账单号
        if (profitShareRecordDO != null) {
            throw new BusinessException("分账单号已经存在，请重新输入");
        }


        // 生成平台分账单号
        String platProfitShareNo = PREFIX_SHARE + StringUtil.getDate(PLAT_ORDER_PART) + StringUtil.generateNonceStr(4);

        /**
         * 保存数据
         *  1、保存分账记录数据
         *  2、保存分账详情数据
         */
        profitShareRecordDO = new ProfitShareRecordDO();
        Date date = new Date();
        Long sysNo = globalSysnoGenerator.nextSysno();
        profitShareRecordDO.setSysNo(sysNo);
        profitShareRecordDO.setOrderNo(orderNo);
        profitShareRecordDO.setPlatOrderNo(platOrderNo);
        profitShareRecordDO.setChannelOrderNo(channelOrderNo);
        profitShareRecordDO.setProfitShareNo(profitShareNo);
        profitShareRecordDO.setPlatProfitShareNo(platProfitShareNo);
        profitShareRecordDO.setMerchantNo(merchantNo);
        profitShareRecordDO.setMerchantName(jWellWXPayConfig.getMerchantPayInfoDO().getMerchantName());
        profitShareRecordDO.setVersion(1);
        profitShareRecordDO.setStatus(ShareRecordStatus.SHARE_HANDING.getCode());
        profitShareRecordDO.setApplyTime(date);
        profitShareRecordDO.setRefundStatus(ShareRefundStatus.SHARE_UN_REFUND.getCode());
        // TODO 费率暂定为0
        profitShareRecordDO.setMerCost(0);
        profitShareRecordDO.setCreateDate(date);

        ProfitShareDetailDO profitShareDetailDO = new ProfitShareDetailDO();
        profitShareDetailDO.setShareRecordSysNo(sysNo);
        profitShareDetailDO.setPlatProfitShareNo(platProfitShareNo);
        profitShareDetailDO.setMerchantNo(merchantNo);
        profitShareDetailDO.setVersion(1);
        profitShareDetailDO.setStatus(ShareDetailStatus.SHARE_HANDING.getCode());
        profitShareDetailDO.setApplyTime(date);
        profitShareDetailDO.setCreateDate(date);

        List<WxProfitShareReceiverReq> wxProfitShareReceiverReqs = wxProfitShareSingleReq.getWxProfitShareReceiverReqs();
        List<WxProfitShareToWxReq> wxProfitShareToWxReqs = new ArrayList<>(10);
        for (WxProfitShareReceiverReq wxProfitShareReceiverReq : wxProfitShareReceiverReqs) {

            WxProfitShareToWxReq wxProfitShareToWxReq = EntityConverter.copyAndGetSingle(
                    wxProfitShareReceiverReq, WxProfitShareToWxReq.class);
            Integer receiverType = wxProfitShareReceiverReq.getReceiverType();
            wxProfitShareToWxReq.setType(WxProfitReceiverType.getByCode(receiverType).getValue());

            wxProfitShareToWxReqs.add(wxProfitShareToWxReq);

            profitShareDetailDO.setSysNo(globalSysnoGenerator.nextSysno());
            profitShareDetailDO.setReceiverType(receiverType);
            profitShareDetailDO.setReceiverAccount(wxProfitShareReceiverReq.getAccount());
            profitShareDetailDO.setAmount(wxProfitShareReceiverReq.getAmount());
            profitShareDetailDO.setDescription(wxProfitShareReceiverReq.getDescription());

            profitShareDetailMapper.insert(profitShareDetailDO);
        }

        JSONArray jsonArray = JSONArray.fromObject(wxProfitShareToWxReqs);
        String receivers = jsonArray.toString();
        profitShareRecordDO.setReceiverInfo(receivers);
        int i = profitShareRecordMapper.insert(profitShareRecordDO);
        if (i < 1) {
            logger.error("分账数据存入数据库的时候失败，请手动处理[" + profitShareRecordDO.toString() + "]");
        }

        // 存放业务参数
        Map<String, String> reqData = new HashMap<>(32);
        reqData.put("out_order_no", platProfitShareNo);
        reqData.put("transaction_id", channelOrderNo);
        reqData.put("receivers", receivers);

        // 将数据发送微信并接受返回数据封装到MAP集合中
        Map<String, String> response = postAndReceiveData(SINGLE_PROFIT_URL, true, reqData, jWellWXPayConfig);
        logger.info("微信单次分账返回信息：" + response);

        // 如果分账成功 记录分账数据库中
        WxSingleProfitShareDTO wxSingleProfitShareDTO = new WxSingleProfitShareDTO();

        String return_code = response.get("return_code");
        String return_msg = response.get("return_msg");
        if (!SUCCESS.equals(return_code)) {
            wxSingleProfitShareDTO.setResultCode(FAIL);
            wxSingleProfitShareDTO.setErrCodeMsg(return_msg);

            profitShareRecordDO.setErrCode(return_code);
            profitShareRecordDO.setErrCodeDes(return_msg);
            profitShareRecordDO.setStatus(ShareRecordStatus.SHARE_FAIL.getCode());
            profitShareRecordMapper.updateRecodeByInput(profitShareRecordDO);

            profitShareDetailDO.setStatus(ShareDetailStatus.SHARE_FAIL.getCode());
            profitShareDetailMapper.updateDetailByInput(profitShareDetailDO);

            throw new BusinessException(return_msg);
        }

        String result_code = response.get("result_code");
        String err_code = response.get("err_code");
        String err_code_des = response.get("err_code_des");
        if (!SUCCESS.equals(result_code)) {

            wxSingleProfitShareDTO.setResultCode(result_code);
            err_code = err_code == null ? result_code : err_code;
            wxSingleProfitShareDTO.setErrCode(err_code);
            wxSingleProfitShareDTO.setErrCodeMsg(err_code_des);

            profitShareRecordDO.setErrCode(err_code);
            profitShareRecordDO.setErrCodeDes(err_code_des);
            profitShareRecordDO.setStatus(ShareRecordStatus.SHARE_FAIL.getCode());
            profitShareRecordMapper.updateRecodeByInput(profitShareRecordDO);

            profitShareDetailDO.setStatus(ShareDetailStatus.SHARE_FAIL.getCode());
            profitShareDetailMapper.updateDetailByInput(profitShareDetailDO);

            throw new BusinessException(err_code_des);
        }

        String channelProfitShareNo = response.get("order_id");
        // 修改分账记录表中的分账状态和完成时间
        profitShareRecordDO.setChannelProfitShareNo(channelProfitShareNo);
        profitShareRecordDO.setStatus(ShareRecordStatus.SHARE_SUCCESS.getCode());
        Date successDate = new Date();
        profitShareRecordDO.setPaySuccessTime(successDate);
        profitShareRecordMapper.updateRecodeByInput(profitShareRecordDO);

        // 修改分账详情表中的分账状态和完成时间
        profitShareDetailDO.setStatus(ShareDetailStatus.SHARE_SUCCESS.getCode());
        profitShareDetailDO.setSuccessTime(successDate);
        profitShareDetailMapper.updateDetailByInput(profitShareDetailDO);

        // 单笔分账需要修改支付记录表中的分账状态为分账完成
        tradePaymentRecordDO.setProfitShareStatus(ProfitShareStatus.SHARE_DONE.getCode());
        tradePaymentRecordMapper.updateRecodeByInput(tradePaymentRecordDO);

        wxSingleProfitShareDTO.setResultCode(SUCCESS);
        wxSingleProfitShareDTO.setOrderNo(response.get(orderNo));
        wxSingleProfitShareDTO.setPlatOrderNo(response.get(platOrderNo));
        wxSingleProfitShareDTO.setProfitShareNo(profitShareNo);
        wxSingleProfitShareDTO.setPlatProfitShareNo(response.get("out_order_no"));

        return wxSingleProfitShareDTO;
    }


    @Override
    public WxSingleProfitShareDTO multiProfitShare(WxProfitShareSingleReq wxProfitShareSingleReq) throws BusinessException {
        return null;
    }

    @Override
    public WxProfitShareQueryDTO profitShareQuery(WxProfitShareQueryReq wxProfitShareQueryReq) throws BusinessException {

        // 获取商户号，验证该商户是否在支付平台存在配置数据
        String merchantNo = wxProfitShareQueryReq.getMerchantNo();
        JWellWXPayConfig jWellWXPayConfig = WxPayHandler.merchantInfoMap.get(merchantNo);
        if (jWellWXPayConfig == null) {
            throw new BusinessException("未获取到商户号为：" + merchantNo + "商户的相关配置信息，" +
                    "请联系相关工作人员进行商户数据确认或新增该商户" + merchantNo + "的配置信息。");
        }

        String orderNo = wxProfitShareQueryReq.getOrderNo();
        TradePaymentRecordDO tradePaymentRecordDO = tradePaymentRecordMapper.selectRecodeByOrderNo(orderNo);
        if (tradePaymentRecordDO == null) {
            throw new BusinessException("输入的订单号在系统中不存在，请确认订单号[" + orderNo + "]是否输入正确");
        }

        String profitShareNo = wxProfitShareQueryReq.getProfitShareNo();
        ProfitShareRecordDO profitShareRecordDO = profitShareRecordMapper.selectRecodeByProfitShareNo(profitShareNo);
        if (profitShareRecordDO == null) {
            throw new BusinessException("输入的分账单号在系统中不存在，请确认分账单号[" + profitShareNo + "]是否输入正确");
        }

        String platProfitShareNo = profitShareRecordDO.getPlatProfitShareNo();
        List<ProfitShareDetailDO> profitShareDetailDOS = profitShareDetailMapper.selectDetailByPlatProfitShareNo(platProfitShareNo);
        boolean detailExist = true;
        if (profitShareDetailDOS == null || profitShareDetailDOS.size() < 1) {
            detailExist = false;
        }

        WxProfitShareQueryDTO wxProfitShareQueryDTO = EntityConverter.copyAndGetSingle(
                profitShareRecordDO, WxProfitShareQueryDTO.class);

        List<WxProfitShareReceiverInfoDTO> wxProfitShareReceiverInfoDTOS = new ArrayList<>(16);

        // 如果分账状态为处理中，或者不存在分账详情记录，则到银行查询
        boolean unProfitShare = profitShareRecordDO.getStatus() == null
                || profitShareRecordDO.getStatus().equals(ShareRecordStatus.SHARE_HANDING.getCode());
        if (unProfitShare || !detailExist) {

            String channelOrderNo = tradePaymentRecordDO.getChannelOrderNo();

            Map<String, String> postData = new HashMap<>(16);
            postData.put("transaction_id", channelOrderNo);
            postData.put("out_order_no", platProfitShareNo);

            // 将数据发送微信并接受返回数据封装到MAP集合中
            Map<String, String> response = postAndReceiveData(PROFIT_QUERY_URL, false, postData, jWellWXPayConfig);
            logger.info("微信查询分账结果返回信息：" + response);

            String return_code = response.get("return_code");
            String return_msg = response.get("return_msg");
            if (!SUCCESS.equals(return_code)) {
                profitShareRecordDO.setErrCode(return_code);
                profitShareRecordDO.setErrCodeDes(return_msg);

                int i = profitShareRecordMapper.updateRecodeByInput(profitShareRecordDO);
                if (i < 1) {
                    throw new BusinessException("更新分账记录数据状态失败，请手动处理数据[" + profitShareRecordDO.toString() + "]");
                }

                wxProfitShareQueryDTO.setResultCode(FAIL);
                wxProfitShareQueryDTO.setErrCode(return_code);
                wxProfitShareQueryDTO.setErrCodeDes(return_msg);

                return wxProfitShareQueryDTO;
            }

            String result_code = response.get("result_code");
            String err_code = response.get("err_code");
            String err_code_des = response.get("err_code_des");
            if (!SUCCESS.equals(result_code)) {
                err_code = err_code == null ? result_code : err_code;
                profitShareRecordDO.setErrCode(err_code);
                profitShareRecordDO.setErrCodeDes(err_code_des);
                profitShareRecordDO.setStatus(ShareRecordStatus.SHARE_FAIL.getCode());

                int i = profitShareRecordMapper.updateRecodeByInput(profitShareRecordDO);
                if (i < 1) {
                    throw new BusinessException("更新分账记录数据状态失败，请手动处理数据[" + profitShareRecordDO.toString() + "]");
                }

                wxProfitShareQueryDTO.setResultCode(FAIL);
                wxProfitShareQueryDTO.setErrCode(err_code);
                wxProfitShareQueryDTO.setStatus(ShareDetailStatus.SHARE_FAIL.getCode());
                wxProfitShareQueryDTO.setErrCodeDes(err_code_des);

                return wxProfitShareQueryDTO;
            }

            String order_id = response.get("order_id");
            Integer amount = Integer.parseInt(response.get("amount"));
            wxProfitShareQueryDTO.setResultCode(SUCCESS);
            wxProfitShareQueryDTO.setStatus(ShareRecordStatus.SHARE_SUCCESS.getCode());
            wxProfitShareQueryDTO.setAmount(amount);
            wxProfitShareQueryDTO.setDescription(response.get("description"));

            profitShareRecordDO.setStatus(ShareRecordStatus.SHARE_SUCCESS.getCode());
            profitShareRecordDO.setChannelProfitShareNo(order_id);
            if (!detailExist) {
                // 分账详情数据不存在则新增

                String receivers = response.get("receivers");
                com.alibaba.fastjson.JSONArray jsonArray = JSONObject.parseArray(receivers);
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    ProfitShareDetailDO profitShareDetailDO = new ProfitShareDetailDO();
                    profitShareDetailDO.setSysNo(globalSysnoGenerator.nextSysno());
                    profitShareDetailDO.setShareRecordSysNo(profitShareRecordDO.getSysNo());
                    profitShareDetailDO.setPlatProfitShareNo(platProfitShareNo);
                    profitShareDetailDO.setMerchantNo(merchantNo);
                    if (WxProfitReceiverType.MERCHANT_ID.getValue().equals(jsonObject.get("type"))) {
                        profitShareDetailDO.setReceiverType(WxProfitReceiverType.MERCHANT_ID.getCode());
                    } else if (WxProfitReceiverType.PERSONAL_WECHATID.getValue().equals(jsonObject.get("type"))) {
                        profitShareDetailDO.setReceiverType(WxProfitReceiverType.PERSONAL_WECHATID.getCode());
                    } else {
                        profitShareDetailDO.setReceiverType(WxProfitReceiverType.PERSONAL_OPENID.getCode());
                    }
                    profitShareDetailDO.setReceiverAccount(response.get("account"));
                    profitShareDetailDO.setDescription(response.get("description"));
                    profitShareDetailDO.setAmount(Integer.parseInt(response.get("amount")));
                    profitShareDetailDO.setVersion(1);
                    profitShareDetailDO.setStatus(ShareDetailStatus.SHARE_SUCCESS.getCode());
                    profitShareDetailDO.setApplyTime(profitShareRecordDO.getApplyTime());
                    String finishTime = response.get("finish_time");
                    profitShareDetailDO.setSuccessTime(StringUtil.formatDateValue(finishTime, PLAT_ORDER_PART));
                    profitShareDetailDO.setCreateDate(new Date());

                    WxProfitShareReceiverInfoDTO wxProfitShareReceiverInfoDTO = EntityConverter.copyAndGetSingle(
                            profitShareDetailDO, WxProfitShareReceiverInfoDTO.class);

                    wxProfitShareReceiverInfoDTOS.add(wxProfitShareReceiverInfoDTO);

                    int insert = profitShareDetailMapper.insert(profitShareDetailDO);
                    if (insert < 1) {
                        throw new BusinessException("更新分账详情数据状态失败，请手动处理数据[" + profitShareDetailDO.toString() + "]");
                    }
                    wxProfitShareQueryDTO.setWxProfitShareReceiverInfoDTOS(wxProfitShareReceiverInfoDTOS);
                }
            } else {
                // 分账详情数据存在进行跟新
                for (ProfitShareDetailDO profitShareDetailDO : profitShareDetailDOS) {

                    profitShareDetailDO.setStatus(ShareDetailStatus.SHARE_SUCCESS.getCode());
                    String finishTime = response.get("finish_time");
                    profitShareDetailDO.setSuccessTime(StringUtil.formatDateValue(finishTime, PLAT_ORDER_PART));
                    profitShareDetailMapper.updateDetailByInput(profitShareDetailDO);

                    WxProfitShareReceiverInfoDTO wxProfitShareReceiverInfoDTO = EntityConverter.copyAndGetSingle(
                            profitShareDetailDO, WxProfitShareReceiverInfoDTO.class);

                    wxProfitShareReceiverInfoDTOS.add(wxProfitShareReceiverInfoDTO);
                }
                wxProfitShareQueryDTO.setWxProfitShareReceiverInfoDTOS(wxProfitShareReceiverInfoDTOS);
            }

        } else {
            // 如果本地分账已经成功/失败，则直接返回数据库查询结果
            wxProfitShareQueryDTO.setResultCode(SUCCESS);

            Integer amount = new Integer(0);
            for (ProfitShareDetailDO profitShareDetailDO : profitShareDetailDOS) {

                WxProfitShareReceiverInfoDTO wxProfitShareReceiverInfoDTO = EntityConverter.copyAndGetSingle(
                        profitShareDetailDO, WxProfitShareReceiverInfoDTO.class);

                wxProfitShareReceiverInfoDTOS.add(wxProfitShareReceiverInfoDTO);

                amount += profitShareDetailDO.getAmount();
            }
            wxProfitShareQueryDTO.setAmount(amount);
            wxProfitShareQueryDTO.setWxProfitShareReceiverInfoDTOS(wxProfitShareReceiverInfoDTOS);
        }

        return wxProfitShareQueryDTO;
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
            if (findReceiver.getStatus() == null || findReceiver.getStatus().equals(ReceiverStatus.REMOVED.getCode())) {
                findReceiver.setStatus(ReceiverStatus.ADD_SUCCESS.getCode());
                int i = profitShareReceiverMapper.updateByPrimaryKeySelective(findReceiver);
                if (i < 1) {
                    logger.error("添加分账接收方时，更新分账接收方状态到数据库中失败，请手动处理[" + findReceiver.toString() + "]");
                }
            }
        } else {
            profitShareReceiverDO.setSysNo(globalSysnoGenerator.nextSysno());
            profitShareReceiverDO.setStatus(ReceiverStatus.ADD_SUCCESS.getCode());
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
        if (!findReceiver.getStatus().equals(ReceiverStatus.REMOVED.getCode())) {
            findReceiver.setStatus(ReceiverStatus.REMOVED.getCode());
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
            // 分账查询组装数据的时候请求参数没有APPID
            if (PROFIT_QUERY_URL.equals(url)) {
                fillMap = fillRequestData(postData, config);
            } else {
                fillMap = wxPay.fillRequestData(postData);
            }
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

    /**
     * 分账查询的时候没有APPID
     *
     * @param reqData
     * @param config
     * @return
     * @throws Exception
     */
    public Map<String, String> fillRequestData(Map<String, String> reqData, JWellWXPayConfig config) throws Exception {
        reqData.put("mch_id", config.getMchID());
        reqData.put("nonce_str", WXPayUtil.generateNonceStr());
        reqData.put("sign_type", "HMAC-SHA256");

        reqData.put("sign", WXPayUtil.generateSignature(reqData, config.getKey(), WXPayConstants.SignType.HMACSHA256));
        return reqData;
    }

}
