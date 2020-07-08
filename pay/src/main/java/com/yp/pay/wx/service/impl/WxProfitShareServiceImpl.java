package com.yp.pay.wx.service.impl;

import com.alibaba.fastjson.JSONObject;
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
import com.yp.pay.wx.config.WxMerchantInfo;
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
public class WxProfitShareServiceImpl extends WxServiceData implements WxProfitShareService {

    Logger logger = LoggerFactory.getLogger(getClass());

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

        return profitShareByType(wxProfitShareSingleReq, SINGLE);
    }

    @Override
    public WxSingleProfitShareDTO multiProfitShare(WxProfitShareSingleReq wxProfitShareSingleReq) throws BusinessException {

        return profitShareByType(wxProfitShareSingleReq, MULTI);
    }

    private WxSingleProfitShareDTO profitShareByType(WxProfitShareSingleReq wxProfitShareSingleReq, String type) throws BusinessException {

        // 获取商户号，验证该商户是否在支付平台存在配置数据
        String merchantNo = wxProfitShareSingleReq.getMerchantNo();
        WxMerchantInfo wxMerchantInfo = getWxMerchantInfoWithCert(merchantNo);

        // 验证订单号是否在平台存在支付数据
        String orderNo = wxProfitShareSingleReq.getOrderNo();
        String platOrderNo;
        String channelOrderNo;
        TradePaymentRecordDO tradePaymentRecordDO;
        if (StringUtils.isNotBlank(orderNo)) {

            tradePaymentRecordDO = tradePaymentRecordMapper.selectRecodeByOrderNo(orderNo, merchantNo);
            if (tradePaymentRecordDO == null) {
                throw new BusinessException("未查询出商户["+merchantNo+"]支付订单号[" + orderNo + "]的支付订单记录，请核实订单号是否输入正确。");
            }
            platOrderNo = tradePaymentRecordDO.getPlatOrderNo();
            channelOrderNo = tradePaymentRecordDO.getChannelOrderNo();

        } else {

            platOrderNo = wxProfitShareSingleReq.getPlatOrderNo();
            tradePaymentRecordDO = tradePaymentRecordMapper.selectRecodeByPlatOrderNo(platOrderNo, merchantNo);

            if (tradePaymentRecordDO == null) {
                throw new BusinessException("未查出商户["+merchantNo+"]的平台支付订单号[" + platOrderNo + "]的支付订单记录，请核实订单号是否输入正确。");
            }
            orderNo = tradePaymentRecordDO.getOrderNo();
            channelOrderNo = tradePaymentRecordDO.getChannelOrderNo();
        }

        // 非分账支付不能进行分账操作
        Integer profitShareSign = tradePaymentRecordDO.getProfitShareSign();
        if (ProfitShareSign.UN_SHARE.getCode().equals(profitShareSign)) {
            throw new BusinessException("该笔平台订单号[" + platOrderNo + "]的支付订单不支持分账。");
        }

        // 分账完成的订单和已经分账回退的订单无法进行分账
        Integer profitShareStatus = tradePaymentRecordDO.getProfitShareStatus();
        if (ProfitShareStatus.SHARE_DONE.getCode().equals(profitShareStatus)) {
            throw new BusinessException("该笔平台订单号[" + platOrderNo + "]的支付订单已完成分账，请勿重复分账。");
        }
        if (ProfitShareStatus.SHARE_REFUND.getCode().equals(profitShareStatus)) {
            throw new BusinessException("该笔平台订单号[" + platOrderNo + "]的支付订单已完成分账回退，请勿再次进行分账。");
        }

        String profitShareNo = wxProfitShareSingleReq.getProfitShareNo();
        ProfitShareRecordDO profitShareRecordDO = profitShareRecordMapper.selectRecodeByProfitShareNo(profitShareNo, merchantNo);
        // 如果当前分账单号的记录在数据库中存在，则提示用户修改分账单号
        if (profitShareRecordDO != null) {
            throw new BusinessException("分账单号已经存在，请重新输入");
        }

        // 生成平台分账单号
        String platProfitShareNo = PREFIX_SHARE + StringUtil.getDate(PLAT_ORDER_PART) + StringUtil.generateNonceStrUpperCase(4);

        /*
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
        profitShareRecordDO.setMerchantName(wxMerchantInfo.getMerchantInfoDO().getMerchantName());
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
            if (wxProfitShareToWxReq == null) {
                throw new BusinessException("调用微信分账接口前，数据转化异常，请检查数据和程序");
            }

            // 将平台类型转化成微信需要的类型
            Integer receiverType = wxProfitShareReceiverReq.getReceiverType();
            WxProfitReceiverType receiverTypeValue = WxProfitReceiverType.getByCode(receiverType);
            if (receiverTypeValue == null) {
                throw new BusinessException("您输入的分账接收方类型有误，请确认后重新输入");
            }
            wxProfitShareToWxReq.setType(receiverTypeValue.getValue());

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
        Map<String, String> response = postAndReceiveData(SINGLE_PROFIT_URL, true, reqData, wxMerchantInfo);
        logger.info("微信单次分账返回信息：" + response);

        // 如果分账成功 记录分账数据库中
        WxSingleProfitShareDTO wxSingleProfitShareDTO = new WxSingleProfitShareDTO();

        String returnCode = response.get("return_code");
        String returnMsg = response.get("return_msg");
        if (!SUCCESS.equals(returnCode)) {
            wxSingleProfitShareDTO.setResultCode(FAIL);
            wxSingleProfitShareDTO.setErrCodeMsg(returnMsg);

            profitShareRecordDO.setErrCode(returnCode);
            profitShareRecordDO.setErrCodeDes(returnMsg);
            profitShareRecordDO.setStatus(ShareRecordStatus.SHARE_FAIL.getCode());
            profitShareRecordMapper.updateRecodeByInput(profitShareRecordDO);

            profitShareDetailDO.setStatus(ShareDetailStatus.SHARE_FAIL.getCode());
            profitShareDetailMapper.updateDetailByPlatShareNo(profitShareDetailDO);

            throw new BusinessException(returnMsg);
        }

        String resultCode = response.get("result_code");
        String errCode = response.get("err_code");
        String errCodeDes = response.get("err_code_des");
        if (!SUCCESS.equals(resultCode)) {

            wxSingleProfitShareDTO.setResultCode(resultCode);
            errCode = errCode == null ? resultCode : errCode;
            wxSingleProfitShareDTO.setErrCode(errCode);
            wxSingleProfitShareDTO.setErrCodeMsg(errCodeDes);

            profitShareRecordDO.setErrCode(errCode);
            profitShareRecordDO.setErrCodeDes(errCodeDes);
            profitShareRecordDO.setStatus(ShareRecordStatus.SHARE_FAIL.getCode());
            profitShareRecordMapper.updateRecodeByInput(profitShareRecordDO);

            profitShareDetailDO.setStatus(ShareDetailStatus.SHARE_FAIL.getCode());
            profitShareDetailMapper.updateDetailByPlatShareNo(profitShareDetailDO);

            throw new BusinessException(errCodeDes);
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
        profitShareDetailMapper.updateDetailByPlatShareNo(profitShareDetailDO);

        // 单笔分账需要修改支付记录表中的分账状态为分账完成
        if (SINGLE.equals(type)) {
            tradePaymentRecordDO.setProfitShareStatus(ProfitShareStatus.SHARE_DONE.getCode());
        } else {
            // 多笔分账，分账后标记为部分分账
            tradePaymentRecordDO.setProfitShareStatus(ProfitShareStatus.SHARE_PART.getCode());
        }
        tradePaymentRecordMapper.updateRecodeByInput(tradePaymentRecordDO);

        wxSingleProfitShareDTO.setResultCode(SUCCESS);
        wxSingleProfitShareDTO.setOrderNo(response.get(orderNo));
        wxSingleProfitShareDTO.setPlatOrderNo(response.get(platOrderNo));
        wxSingleProfitShareDTO.setProfitShareNo(profitShareNo);
        wxSingleProfitShareDTO.setPlatProfitShareNo(response.get("out_order_no"));

        return wxSingleProfitShareDTO;
    }

    @Override
    public WxProfitShareQueryDTO profitShareQuery(WxProfitShareQueryReq wxProfitShareQueryReq) throws BusinessException {

        // 获取商户号，验证该商户是否在支付平台存在配置数据
        String merchantNo = wxProfitShareQueryReq.getMerchantNo();
        WxMerchantInfo wxMerchantInfo = getWxMerchantInfoWithCert(merchantNo);

        String orderNo = wxProfitShareQueryReq.getOrderNo();
        TradePaymentRecordDO tradePaymentRecordDO = tradePaymentRecordMapper.selectRecodeByOrderNo(orderNo, merchantNo);
        if (tradePaymentRecordDO == null) {
            throw new BusinessException("输入的订单号在系统中不存在，请确认订单号[" + orderNo + "]是否输入正确");
        }

        String profitShareNo = wxProfitShareQueryReq.getProfitShareNo();
        ProfitShareRecordDO profitShareRecordDO = profitShareRecordMapper.selectRecodeByProfitShareNo(profitShareNo, merchantNo);
        if (profitShareRecordDO == null) {
            throw new BusinessException("输入的分账单号在系统中不存在，请确认分账单号[" + profitShareNo + "]是否输入正确");
        }

        String platProfitShareNo = profitShareRecordDO.getPlatProfitShareNo();
        List<ProfitShareDetailDO> profitShareDetailList = profitShareDetailMapper.selectDetailByPlatProfitShareNo(platProfitShareNo,merchantNo);
        boolean detailExist = true;
        if (profitShareDetailList == null || profitShareDetailList.size() < 1) {
            detailExist = false;
        }

        WxProfitShareQueryDTO wxProfitShareQueryDTO = EntityConverter.copyAndGetSingle(
                profitShareRecordDO, WxProfitShareQueryDTO.class);
        if (wxProfitShareQueryDTO == null) {
            throw new BusinessException("分账单查询中，对象转化异常！");
        }

        List<WxProfitShareReceiverInfoDTO> wxProfitShareReceiverList = new ArrayList<>(16);

        // 如果分账状态为处理中，或者不存在分账详情记录，则到银行查询
        boolean unProfitShare = profitShareRecordDO.getStatus() == null
                || profitShareRecordDO.getStatus().equals(ShareRecordStatus.SHARE_HANDING.getCode());
        if (unProfitShare || !detailExist) {

            String channelOrderNo = tradePaymentRecordDO.getChannelOrderNo();

            Map<String, String> postData = new HashMap<>(16);
            postData.put("transaction_id", channelOrderNo);
            postData.put("out_order_no", platProfitShareNo);

            // 将数据发送微信并接受返回数据封装到MAP集合中
            Map<String, String> response = postAndReceiveData(PROFIT_QUERY_URL, false, postData, wxMerchantInfo);
            logger.info("微信查询分账结果返回信息：" + response);

            String returnCode = response.get("return_code");
            String returnMsg = response.get("return_msg");
            if (!SUCCESS.equals(returnCode)) {
                profitShareRecordDO.setErrCode(returnCode);
                profitShareRecordDO.setErrCodeDes(returnMsg);

                int i = profitShareRecordMapper.updateRecodeByInput(profitShareRecordDO);
                if (i < 1) {
                    throw new BusinessException("更新分账记录数据状态失败，请手动处理数据[" + profitShareRecordDO.toString() + "]");
                }

                wxProfitShareQueryDTO.setResultCode(FAIL);
                wxProfitShareQueryDTO.setErrCode(returnCode);
                wxProfitShareQueryDTO.setErrCodeDes(returnMsg);

                return wxProfitShareQueryDTO;
            }

            String resultCode = response.get("result_code");
            String errCode = response.get("err_code");
            String errCodeDes = response.get("err_code_des");
            if (!SUCCESS.equals(resultCode)) {
                errCode = errCode == null ? resultCode : errCode;
                profitShareRecordDO.setErrCode(errCode);
                profitShareRecordDO.setErrCodeDes(errCodeDes);
                profitShareRecordDO.setStatus(ShareRecordStatus.SHARE_FAIL.getCode());

                int i = profitShareRecordMapper.updateRecodeByInput(profitShareRecordDO);
                if (i < 1) {
                    throw new BusinessException("更新分账记录数据状态失败，请手动处理数据[" + profitShareRecordDO.toString() + "]");
                }

                wxProfitShareQueryDTO.setResultCode(FAIL);
                wxProfitShareQueryDTO.setErrCode(errCode);
                wxProfitShareQueryDTO.setStatus(ShareDetailStatus.SHARE_FAIL.getCode());
                wxProfitShareQueryDTO.setErrCodeDes(errCodeDes);

                return wxProfitShareQueryDTO;
            }

            String orderId = response.get("order_id");
            Integer amount = Integer.parseInt(response.get("amount"));
            wxProfitShareQueryDTO.setResultCode(SUCCESS);
            wxProfitShareQueryDTO.setStatus(ShareRecordStatus.SHARE_SUCCESS.getCode());
            wxProfitShareQueryDTO.setAmount(amount);
            wxProfitShareQueryDTO.setDescription(response.get("description"));

            profitShareRecordDO.setStatus(ShareRecordStatus.SHARE_SUCCESS.getCode());
            profitShareRecordDO.setChannelProfitShareNo(orderId);
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

                    wxProfitShareReceiverList.add(wxProfitShareReceiverInfoDTO);

                    int insert = profitShareDetailMapper.insert(profitShareDetailDO);
                    if (insert < 1) {
                        throw new BusinessException("更新分账详情数据状态失败，请手动处理数据[" + profitShareDetailDO.toString() + "]");
                    }
                    wxProfitShareQueryDTO.setWxProfitShareReceiverInfoDTOS(wxProfitShareReceiverList);
                }
            } else {
                // 分账详情数据存在进行跟新
                for (ProfitShareDetailDO profitShareDetailDO : profitShareDetailList) {

                    profitShareDetailDO.setStatus(ShareDetailStatus.SHARE_SUCCESS.getCode());
                    String finishTime = response.get("finish_time");
                    profitShareDetailDO.setSuccessTime(StringUtil.formatDateValue(finishTime, PLAT_ORDER_PART));
                    profitShareDetailMapper.updateDetailByInput(profitShareDetailDO);

                    WxProfitShareReceiverInfoDTO wxProfitShareReceiverInfoDTO = EntityConverter.copyAndGetSingle(
                            profitShareDetailDO, WxProfitShareReceiverInfoDTO.class);

                    wxProfitShareReceiverList.add(wxProfitShareReceiverInfoDTO);
                }
                wxProfitShareQueryDTO.setWxProfitShareReceiverInfoDTOS(wxProfitShareReceiverList);
            }

        } else {
            // 如果本地分账已经成功/失败，则直接返回数据库查询结果
            wxProfitShareQueryDTO.setResultCode(SUCCESS);

            Integer amount = 0;
            for (ProfitShareDetailDO profitShareDetailDO : profitShareDetailList) {

                WxProfitShareReceiverInfoDTO wxProfitShareReceiverInfoDTO = EntityConverter.copyAndGetSingle(
                        profitShareDetailDO, WxProfitShareReceiverInfoDTO.class);

                wxProfitShareReceiverList.add(wxProfitShareReceiverInfoDTO);

                amount += profitShareDetailDO.getAmount();
            }
            wxProfitShareQueryDTO.setAmount(amount);
            wxProfitShareQueryDTO.setWxProfitShareReceiverInfoDTOS(wxProfitShareReceiverList);
        }

        return wxProfitShareQueryDTO;
    }

    @Override
    public WxProfitShareReceiverAddDTO profitShareAddReceiver(WxProfitShareReceiverAddReq wxProfitShareReceiverAddReq) throws BusinessException {

        // 获取商户号，验证该商户是否在支付平台存在配置数据
        String merchantNo = wxProfitShareReceiverAddReq.getMerchantNo();
        WxMerchantInfo wxMerchantInfo = getWxMerchantInfoWithCert(merchantNo);

        Integer type = wxProfitShareReceiverAddReq.getReceiverType();
        if (type == null) {
            throw new BusinessException("[分账接收方类型]参数不能为空。");
        }

        Integer relationType = wxProfitShareReceiverAddReq.getRelationType();
        if (relationType == null) {
            throw new BusinessException("[与分账方的关系类型]参数不能为空。");
        }

        JSONObject reqData = new JSONObject();

        WxProfitReceiverType receiverType = WxProfitReceiverType.getByCode(type);
        if (receiverType == null) {
            throw new BusinessException("您输入的分账接收方类型不存在，请确认输入类型。");
        }
        reqData.put("type", receiverType.getValue());
        reqData.put("account", wxProfitShareReceiverAddReq.getReceiverAccount());
        reqData.put("name", wxProfitShareReceiverAddReq.getReceiverName());
        WxRelationWithReceiver value = WxRelationWithReceiver.getByCode(relationType);
        if (value == null) {
            throw new BusinessException("您输入的分账方与商户的关系类型不存在，请确认输入类型。");
        }
        reqData.put("relation_type", value.getValue());
        reqData.put("custom_relation", wxProfitShareReceiverAddReq.getCustomRelation());

        Map<String, String> postData = new HashMap<>(16);
        postData.put("receiver", reqData.toJSONString());

        // 将数据发送微信并接受返回数据封装到MAP集合中
        Map<String, String> response = postAndReceiveData(PROFIT_RECEIVER_ADD_URL, false, postData, wxMerchantInfo);
        logger.info("微信添加分账接收方返回信息：" + response);

        // 如果添加分账用户成功则记录数据库中
        WxProfitShareReceiverAddDTO wxProfitShareReceiverAddDTO = new WxProfitShareReceiverAddDTO();

        String returnCode = response.get("return_code");
        String returnMsg = response.get("return_msg");
        if (!SUCCESS.equals(returnCode)) {
            wxProfitShareReceiverAddDTO.setResultCode(FAIL);
            wxProfitShareReceiverAddDTO.setErrCodeMsg(returnMsg);
            throw new BusinessException(returnMsg);
        }

        String resultCode = response.get("result_code");
        String errCode = response.get("err_code");
        String errCodeDes = response.get("err_code_des");
        if (!SUCCESS.equals(resultCode)) {

            wxProfitShareReceiverAddDTO.setResultCode(resultCode);
            wxProfitShareReceiverAddDTO.setErrCode(errCode);
            wxProfitShareReceiverAddDTO.setErrCodeMsg(errCodeDes);

            throw new BusinessException(errCodeDes);
        }

        ProfitShareReceiverDO profitShareReceiverDO = EntityConverter.copyAndGetSingle(
                wxProfitShareReceiverAddReq, ProfitShareReceiverDO.class);
        if (profitShareReceiverDO == null) {
            throw new BusinessException("数据转化异常");
        }
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
        WxMerchantInfo wxMerchantInfo = getWxMerchantInfoWithCert(merchantNo);

        Integer type = wxProfitShareReceiverRemoveReq.getReceiverType();
        if (type == null) {
            throw new BusinessException("[分账接收方类型]参数不能为空。");
        }

        JSONObject reqData = new JSONObject();
        WxProfitReceiverType receiverType = WxProfitReceiverType.getByCode(type);
        if (receiverType == null) {
            throw new BusinessException("您输入的分账接收方类型不存在。");
        }
        reqData.put("type", receiverType.getValue());
        reqData.put("account", wxProfitShareReceiverRemoveReq.getReceiverAccount());

        Map<String, String> postData = new HashMap<>(16);
        postData.put("receiver", reqData.toJSONString());

        // 将数据发送微信并接受返回数据封装到MAP集合中
        Map<String, String> response = postAndReceiveData(PROFIT_RECEIVER_REMOVE_URL, false, postData, wxMerchantInfo);
        logger.info("微信删除分账接收方返回信息：" + response);

        // 如果添加分账用户成功则记录数据库中 将数据库数据进行删除操作
        WxProfitShareReceiverAddDTO wxProfitShareReceiverAddDTO = new WxProfitShareReceiverAddDTO();

        String returnCode = response.get("return_code");
        String returnMsg = response.get("return_msg");
        if (!SUCCESS.equals(returnCode)) {
            wxProfitShareReceiverAddDTO.setResultCode(FAIL);
            wxProfitShareReceiverAddDTO.setErrCodeMsg(returnMsg);
            throw new BusinessException(returnMsg);
        }

        String resultCode = response.get("result_code");
        String errCode = response.get("err_code");
        String errCodeDes = response.get("err_code_des");
        if (!SUCCESS.equals(resultCode)) {

            wxProfitShareReceiverAddDTO.setResultCode(resultCode);
            wxProfitShareReceiverAddDTO.setErrCode(errCode);
            wxProfitShareReceiverAddDTO.setErrCodeMsg(errCodeDes);

            throw new BusinessException(errCodeDes);
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

        // 获取商户号，验证该商户是否在支付平台存在配置数据
        String merchantNo = wxProfitShareFinishReq.getMerchantNo();
        WxMerchantInfo wxMerchantInfo = getWxMerchantInfoWithCert(merchantNo);

        String orderNo = wxProfitShareFinishReq.getOrderNo();
        TradePaymentRecordDO tradePaymentRecordDO = tradePaymentRecordMapper.selectRecodeByOrderNo(orderNo, merchantNo);
        if (tradePaymentRecordDO == null) {
            throw new BusinessException("未查询到订单号为[" + orderNo + "]的订单，请核实订单数据是否输入正确。");
        }
        Integer profitShareStatus = tradePaymentRecordDO.getProfitShareStatus();
        if (ProfitShareStatus.SHARE_DONE.getCode().equals(profitShareStatus)) {
            throw new BusinessException("订单号为[" + orderNo + "]的订单，已完成分账，请勿再次操作。");
        }
        if (ProfitShareStatus.SHARE_REFUND.getCode().equals(profitShareStatus)) {
            throw new BusinessException("订单号为[" + orderNo + "]的订单，已完成分账回退，无法进行完成分账操作。");
        }

        String profitShareNo = wxProfitShareFinishReq.getProfitShareNo();
        ProfitShareRecordDO profitShareRecordDO = profitShareRecordMapper.selectRecodeByProfitShareNo(profitShareNo, merchantNo);
        if (profitShareRecordDO == null) {
            throw new BusinessException("未查询到商户分账单号为[" + profitShareNo + "]的分账订单，请核实分账订单数据是否输入正确。");
        }
        String orderNoInShare = profitShareRecordDO.getOrderNo();
        if (!orderNo.equals(orderNoInShare)) {
            throw new BusinessException("查询到商户分账单号为[" + profitShareNo + "]的分账订单与支付订单号[" + orderNo + "]的订单不是对应数据，无法分账完成。");
        }
        Integer status = profitShareRecordDO.getStatus();
        if (!ShareRecordStatus.SHARE_SUCCESS.getCode().equals(status)) {
            throw new BusinessException("商户分账单号为[" + profitShareNo + "]的分账订单还未分账成功，故无法进行完结分账操作。");
        }
        Integer refundStatus = profitShareRecordDO.getRefundStatus();
        if (!ShareRefundStatus.SHARE_REFUND.getCode().equals(refundStatus)) {
            throw new BusinessException("商户分账单号为[" + profitShareNo + "]的分账订单已经完成分账回退，故无法进行完结分账操作。");
        }

        String channelOrderNo = tradePaymentRecordDO.getChannelOrderNo();
        String platProfitShareNo = profitShareRecordDO.getPlatProfitShareNo();

        Map<String, String> postData = new HashMap<>(16);
        postData.put("transaction_id", channelOrderNo);
        postData.put("out_order_no", platProfitShareNo);
        postData.put("description", wxProfitShareFinishReq.getDescription());

        // 将数据发送微信并接受返回数据封装到MAP集合中
        Map<String, String> response = postAndReceiveData(PROFIT_QUERY_URL, true, postData, wxMerchantInfo);
        logger.info("微信完结分账接口返回信息：" + response);

        String returnCode = response.get("return_code");
        String returnMsg = response.get("return_msg");

        WxSingleProfitShareDTO wxSingleProfitShareDTO = new WxSingleProfitShareDTO();
        if (!SUCCESS.equals(returnCode)) {

            wxSingleProfitShareDTO.setResultCode(FAIL);
            wxSingleProfitShareDTO.setErrCode(returnCode);
            wxSingleProfitShareDTO.setErrCodeMsg(returnMsg);

            return wxSingleProfitShareDTO;
        }

        String resultCode = response.get("result_code");
        String errCode = response.get("err_code");
        String errCodeDes = response.get("err_code_des");
        if (!SUCCESS.equals(resultCode)) {

            wxSingleProfitShareDTO.setResultCode(FAIL);
            wxSingleProfitShareDTO.setErrCode(errCode);
            wxSingleProfitShareDTO.setErrCodeMsg(errCodeDes);

            return wxSingleProfitShareDTO;
        }

        tradePaymentRecordDO.setProfitShareStatus(ProfitShareStatus.SHARE_DONE.getCode());
        int i = tradePaymentRecordMapper.updateRecodeByInput(tradePaymentRecordDO);
        if (i < 1) {
            logger.error("完结分账时，数据修改状态失败，请手动处理数据[" + tradePaymentRecordDO + "]");
        }
        wxSingleProfitShareDTO.setResultCode(SUCCESS);
        wxSingleProfitShareDTO.setOrderNo(orderNo);
        wxSingleProfitShareDTO.setPlatOrderNo(tradePaymentRecordDO.getPlatOrderNo());
        wxSingleProfitShareDTO.setProfitShareNo(profitShareNo);
        wxSingleProfitShareDTO.setPlatProfitShareNo(platProfitShareNo);

        return wxSingleProfitShareDTO;
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
