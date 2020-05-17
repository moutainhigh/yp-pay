package com.yp.pay.ali.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.demo.trade.model.builder.AlipayTradeQueryRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPayResult;
import com.alipay.demo.trade.model.result.AlipayF2FQueryResult;
import com.yp.pay.ali.handler.AlipayHandler;
import com.yp.pay.ali.service.AliPayService;
import com.yp.pay.base.exception.BusinessException;
import com.yp.pay.common.enums.AccountStatus;
import com.yp.pay.common.enums.PayTypeCodeEnum;
import com.yp.pay.common.enums.TradeStatus;
import com.yp.pay.common.util.EntityConverter;
import com.yp.pay.common.util.GlobalSysnoGenerator;
import com.yp.pay.common.util.StringUtil;
import com.yp.pay.entity.aliandwx.entity.MerchantPayInfoDO;
import com.yp.pay.entity.aliandwx.entity.TradePaymentRecordDO;
import com.yp.pay.entity.aliandwx.dto.MerchantInfoDTO;
import com.yp.pay.entity.aliandwx.dto.TradePaymentRecordDTO;
import com.yp.pay.entity.aliandwx.req.*;
import com.yp.pay.wx.mapper.MerchantPayInfoMapper;
import com.yp.pay.wx.mapper.TradePaymentRecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * @author: lijiang
 * @date: 2019.12.11 15:54
 * @description: AliPayServiceImpl
 */
@Service
public class AliPayServiceImpl implements AliPayService {

    @Autowired
    private MerchantPayInfoMapper merchantPayInfoMapper;

    @Autowired
    private TradePaymentRecordMapper tradePaymentRecordMapper;

    @Autowired
    private GlobalSysnoGenerator globalSysnoGenerator;

    @Autowired
    private AlipayHandler alipayHandler;

    private final static String ALI_SUCCESS_CODE = "10000";

    private final static String ALI_NOT_EXIST_CODE = "ACQ.TRADE_NOT_EXIST";

    private static final Logger logger = LoggerFactory.getLogger(AliPayServiceImpl.class);

    @Override
    public String scanningPay(AliF2FPayReq req) throws BusinessException {

        //校验参数合法性
        Example example = new Example(MerchantPayInfoDO.class);
        example.createCriteria().andEqualTo("merchantNo", req.getMerchantNo())
                .andEqualTo("payWayCode", req.getPayWayCode());

        List<MerchantPayInfoDO> merchants = merchantPayInfoMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(merchants)) {
            throw new BusinessException("商户不存在");
        }
        MerchantPayInfoDO merchant = merchants.get(0);
        if(!AccountStatus.ACTIVE.getCode().equals(merchant.getStatus())){
            throw new BusinessException("商户被冻结，无法收款");
        }

        req.setOutTradeNo(req.getMerchantNo() + System.currentTimeMillis());
        req.setDiscountableAmount(new BigDecimal("0.00"));
        req.setUndiscountableAmount(new BigDecimal("0.00"));

        if(req.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0){
            throw new BusinessException("金额不能为负");
        }
        req.setTotalAmount(req.getTotalAmount().setScale(2, RoundingMode.DOWN));

        if(merchant.getMaxOrderAmount() != null && req.getTotalAmount().compareTo(merchant.getMaxOrderAmount()) > 0){
            throw new BusinessException("商户收款金额超过限额");
        }
        req.setPayTypeCode(PayTypeCodeEnum.ALI_F2F_PAY.getCode());


        //记录到record表
        TradePaymentRecordDO record = handlerPaymentRecord(req, merchant);

        alipayHandler.initConfigs(merchant);
        AlipayF2FPayResult result = alipayHandler.aliF2FPay(req, merchant);
        //解析返回结果
        switch (result.getTradeStatus()){
            case SUCCESS:
                record.setStatus(TradeStatus.SUCCESS.getCode());
                record.setPaySuccessTime(result.getResponse().getGmtPayment());
                //TODO: 为空则为当前时间，需要用户输入密码
                record.setChannelOrderNo(result.getResponse().getTradeNo());
                record.setPaySuccessTime(new Date());
                break;
            case FAILED:
                record.setStatus(TradeStatus.FAIL.getCode());
                record.setErrCode(result.getResponse().getSubCode());
                record.setErrCodeDes(result.getResponse().getSubMsg());
                record.setPaySuccessTime(new Date());
                break;
            case UNKNOWN:
                record.setStatus(TradeStatus.HANDING.getCode());
                break;
            default:
                throw new BusinessException("交易状态异常");
        }

        Example updateExample = new Example(TradePaymentRecordDO.class);
        updateExample.createCriteria().andEqualTo("sysno", record.getSysNo())
                .andEqualTo("version", record.getVersion())
                .andEqualTo("merchantOrderNo", record.getMerchantOrderNo());
        record.setVersion(record.getVersion() + 1);
        tradePaymentRecordMapper.updateByExampleSelective(record, updateExample);
        if(!TradeStatus.SUCCESS.getCode().equals(record.getStatus())){
            throw new BusinessException("交易失败");
        }
        return record.getMerchantOrderNo();
    }

    @Override
    public void queryScanningPayResultJob() {
        Example queryExample = new Example(TradePaymentRecordDO.class);
        Date now = new Date();
        queryExample.createCriteria().andEqualTo("status", TradeStatus.HANDING.getCode())
                .andBetween("createDate", StringUtil.addHours(now, -2), now);
        List<TradePaymentRecordDO> records = tradePaymentRecordMapper.selectByExample(queryExample);
        if(!CollectionUtils.isEmpty(records)){
            logger.info("查询到{}条状态未知的订单,分别为{}", records.size(), JSONObject.toJSONString(records));
            records.forEach(record -> {
                Example queryMerchant = new Example(MerchantPayInfoDO.class);
                queryMerchant.createCriteria().andEqualTo("merchantNo", record.getMerchantNo());
                MerchantPayInfoDO merchantInfo = merchantPayInfoMapper.selectOneByExample(queryMerchant);
                AlipayTradeQueryRequestBuilder queryBuiler = new AlipayTradeQueryRequestBuilder()
                        .setOutTradeNo(record.getMerchantOrderNo());
                try {
                    alipayHandler.initConfigs(merchantInfo);
                    AlipayF2FQueryResult result = alipayHandler.queryAliF2FResult(queryBuiler);
                    switch (result.getTradeStatus()){
                        case SUCCESS:
                            record.setPaySuccessTime(new Date());
                            record.setPaySuccessTime(new Date());
                            record.setChannelOrderNo(result.getResponse().getTradeNo());
                            record.setStatus(TradeStatus.SUCCESS.getCode());
                            break;
                        case FAILED:
                            record.setErrCode(result.getResponse().getSubCode());
                            record.setErrCodeDes(result.getResponse().getSubMsg());
                            record.setPaySuccessTime(new Date());
                            record.setChannelOrderNo(result.getResponse().getTradeNo());
                            record.setStatus(TradeStatus.FAIL.getCode());
                            break;
                        case UNKNOWN:
                            record.setStatus(TradeStatus.HANDING.getCode());
                            record.setErrCode(result.getResponse().getSubCode());
                            record.setErrCodeDes(result.getResponse().getSubMsg());
                            break;
                    }
                    Example updateExample = new Example(TradePaymentRecordDO.class);
                    updateExample.createCriteria().andEqualTo("sysno", record.getSysNo())
                            .andEqualTo("version", record.getVersion())
                            .andEqualTo("merchantOrderNo", record.getMerchantOrderNo());
                    record.setVersion(record.getVersion() + 1);
                    tradePaymentRecordMapper.updateByExampleSelective(record, updateExample);
                } catch (BusinessException e) {
                    e.printStackTrace();
                }

            });
        }
    }

    @Override
    public TradePaymentRecordDTO getScanningPayPlatResult(AliOrderQueryReq aliOrderQueryReq) throws BusinessException {

        String merchantNo = aliOrderQueryReq.getMerchantNo();
        String merchantOrderNo = aliOrderQueryReq.getOrderNo();

        if(StringUtils.isEmpty(merchantNo) || StringUtils.isEmpty(merchantOrderNo)){
            throw new BusinessException("商户号和订单号不能为空");
        }
        Example example = new Example(TradePaymentRecordDO.class);
        example.createCriteria().andEqualTo("merchantOrderNo", merchantOrderNo)
                                .andEqualTo("merchantNo", merchantNo);
        TradePaymentRecordDO exist = tradePaymentRecordMapper.selectOneByExample(example);
        return EntityConverter.copyAndGetSingle(exist, TradePaymentRecordDTO.class);
    }

    @Override
    public String webPay(AliWebPayReq req) throws BusinessException {
        req.setProductCode("QUICK_WAP_WAY");
        if(StringUtils.isEmpty(req.getPayTypeCode())){
            req.setPayTypeCode("ALI_WEB_PAY");
        }
        //校验
        Example example = new Example(MerchantPayInfoDO.class);
        example.createCriteria().andEqualTo("merchantNo", req.getMerchantNo())
        .andEqualTo("payWayCode", "ALI_PAY");
        //获取商户
        MerchantPayInfoDO merchant = merchantPayInfoMapper.selectOneByExample(example);
        //1.校验商户状态
        if(AccountStatus.FREEZE.getCode().equals(merchant.getStatus())){
            throw new BusinessException("该商户支付宝支付配置被冻结，无法支付！");
        }
        //2.校验商户支付配置信息是否齐全
        if(StringUtils.isEmpty(merchant.getAppId())|| StringUtils.isEmpty(merchant.getNotifyUrl()) ||
                StringUtils.isEmpty(merchant.getRsaPrivateKey()) || StringUtils.isEmpty(merchant.getRsaPublicKey()) ||
                StringUtils.isEmpty(merchant.getReturnUrl()) || StringUtils.isEmpty(merchant.getPartnerId()) ||
                StringUtils.isEmpty(merchant.getPartnerKey())){
            throw new BusinessException("商户<"+merchant.getMerchantName()+">支付宝支付配置信息有误！无法支付");
        }
        //3.处理支付记录，校验订单号是否重复
        Example recordExample = new Example(TradePaymentRecordDO.class);
        recordExample.createCriteria().andEqualTo("merchantOrderNo", req.getOutTradeNo());
        TradePaymentRecordDO existOrder = tradePaymentRecordMapper.selectOneByExample(recordExample);
        if(existOrder == null){
            throw new BusinessException("订单号不存在，请检查订单是否通过统一下单创建");
        }
        //TODO: 临时校验，只校验终态
        if(TradeStatus.SUCCESS.getCode().equals(existOrder.getStatus())){
            throw new BusinessException("订单"+existOrder.getMerchantOrderNo()+"已经支付成功，请重新生成订单！");
        }
        if(TradeStatus.FAIL.getCode().equals(existOrder.getStatus())){
            throw new BusinessException("订单"+existOrder.getMerchantOrderNo()+"已经支付失败，请重新生成订单！");
        }
        if(TradeStatus.CLOSED.getCode().equals(existOrder.getStatus())){
            throw new BusinessException("订单"+existOrder.getMerchantOrderNo()+"已经关闭，请重新生成订单！");
        }
        if(existOrder.getQrCodeStatus() != null && existOrder.getQrCodeStatus() == 3){
            throw new BusinessException("该订单已被取消，不能支付，请重新生成二维码。");
        }

        String result = alipayHandler.aliWebPay(req , merchant);
        if(!StringUtils.isEmpty(result)){
            Callable<Integer> call = () -> {
                // 修改状态订单
                Example updateExample = new Example(TradePaymentRecordDO.class);
                updateExample.createCriteria().andEqualTo("sysno", existOrder.getSysNo())
                        .andEqualTo("version", existOrder.getVersion())
                        .andEqualTo("merchantOrderNo", existOrder.getMerchantOrderNo());
                existOrder.setPayWayCode(req.getPayWayCode());
                existOrder.setPayTypeCode(req.getPayTypeCode());
                existOrder.setStatus(TradeStatus.HANDING.getCode());
                existOrder.setQrCodeStatus(1);
                existOrder.setVersion(existOrder.getVersion() + 1);
                tradePaymentRecordMapper.updateByExampleSelective(existOrder, updateExample);
                return 1;
            };
            FutureTask<Integer> task = new FutureTask<>(call);
            Thread thread = new Thread(task);
            thread.start();
        }
        return result;
    }

    @Override
    public TradePaymentRecordDTO webPayQuery(AliWebQueryReq req) throws BusinessException {
        Example merExample = new Example(MerchantPayInfoDO.class);
        merExample.createCriteria().andEqualTo("merchantNo", req.getMerchantNo())
                .andEqualTo("payWayCode", "ALI_PAY");
        MerchantPayInfoDO merchant = merchantPayInfoMapper.selectOneByExample(merExample);
        if(merchant == null){
            throw new BusinessException("商户号对应支付宝商户不存在，请检查");
        }
        Example orderExample = new Example(TradePaymentRecordDO.class);
        orderExample.createCriteria().andEqualTo("merchantOrderNo", req.getOutTradeNo())
                .andEqualTo("payWayCode", "ALI_PAY");
        TradePaymentRecordDO order = tradePaymentRecordMapper.selectOneByExample(orderExample);
        if(order == null){
            throw new BusinessException("订单号为"+req.getOutTradeNo()+"的对应订单不存在");
        }
        if(TradeStatus.HANDING.getCode().equals(order.getStatus())){
            //当订单状态为处理中时，去查询渠道并更新状态
            AlipayTradeQueryResponse resp = alipayHandler.aliWebQueryOrder(req, merchant);
            if(resp != null && ALI_SUCCESS_CODE.equals(resp.getCode())){
                Boolean flag = false;
                switch (resp.getTradeStatus()){
                    case "TRADE_SUCCESS":
                    case "TRADE_FINISHED":
                        order.setStatus(TradeStatus.SUCCESS.getCode());
                        flag = true;
                        order.setChannelOrderNo(resp.getTradeNo());
                        order.setPaySuccessTime(StringUtil.parseDate(resp.getSendPayDate()));
                        order.setPaySuccessTime(new Date());
                        order.setRemark(resp.getBuyerUserId() + "-" + resp.getBuyerLogonId());
                        order.setModifyUser("查询支付宝订单完成");
                        break;
                    case "TRADE_CLOSED":
                        order.setStatus(TradeStatus.CLOSED.getCode());
                        flag = true;
                        order.setChannelOrderNo(resp.getTradeNo());
                        order.setPaySuccessTime(new Date());
                        order.setRemark(resp.getBuyerUserId() + "-" + resp.getBuyerLogonId());
                        order.setModifyUser("查询支付宝订单关闭");
                        break;
                    case "WAIT_BUYER_PAY":
                        order.setErrCode(resp.getTradeStatus());
                        order.setErrCodeDes("订单已创建，等待买家付款");
                        break;
                    default:
                        break;
                }

                if(flag){
                    //获取到订单终态，另起线程进行更新
                    Callable<Integer> call = () -> {
                        Example updateExample = new Example(TradePaymentRecordDO.class);
                        updateExample.createCriteria().andEqualTo("sysno", order.getSysNo())
                                .andEqualTo("version", order.getVersion())
                                .andEqualTo("merchantOrderNo", order.getMerchantOrderNo());
                        order.setVersion(order.getVersion() + 1);
                        int row = tradePaymentRecordMapper.updateByExampleSelective(order, updateExample);
                        if(row != 1){
                            logger.error("查询渠道更新数据库失败，订单号为" + order.getMerchantOrderNo());
                        }
                        return 1;
                    };
                    FutureTask<Integer> task = new FutureTask<>(call);
                    Thread thread = new Thread(task);
                    thread.start();
                }
            }else if(resp != null && ALI_NOT_EXIST_CODE.equals(resp.getSubCode())){
                logger.error("查询支付宝订单号{}渠道返回:{}", req.getOutTradeNo(), resp.getSubMsg());
            }else {
                throw new BusinessException("查询支付宝订单号"+req.getOutTradeNo()+"渠道返回:" + resp.getSubMsg());
            }
        }
        return EntityConverter.copyAndGetSingle(order, TradePaymentRecordDTO.class);
    }

    @Override
    public MerchantInfoDTO queryMerchantByPayCode(MerchantQueryReq req) {
        Example merExample = new Example(MerchantPayInfoDO.class);
        merExample.createCriteria().andEqualTo("merchantNo", req.getMerchantNo())
                .andEqualTo("payWayCode", req.getPayWayCode());
        MerchantPayInfoDO merchant = merchantPayInfoMapper.selectOneByExample(merExample);
        return EntityConverter.copyAndGetSingle(merchant, MerchantInfoDTO.class);
    }

    @Override
    public Boolean aliCancelPay(AliCancelPayReq req) throws BusinessException {
        Example merExample = new Example(MerchantPayInfoDO.class);
        merExample.createCriteria().andEqualTo("merchantNo", req.getMerchantNo())
                .andEqualTo("payWayCode", "ALI_PAY");
        MerchantPayInfoDO merchant = merchantPayInfoMapper.selectOneByExample(merExample);
        if(merchant == null){
            throw new BusinessException("商户号对应支付宝商户不存在，请检查");
        }
        Example orderExample = new Example(TradePaymentRecordDO.class);
        orderExample.createCriteria().andEqualTo("merchantOrderNo", req.getOrderNo())
                .andEqualTo("payWayCode", "ALI_PAY");
        TradePaymentRecordDO order = tradePaymentRecordMapper.selectOneByExample(orderExample);
        if(order == null){
            throw new BusinessException("订单号为"+req.getOrderNo()+"的对应订单不存在");
        }

        Example updateExample = new Example(TradePaymentRecordDO.class);
        updateExample.createCriteria().andEqualTo("sysno", order.getSysNo())
                .andEqualTo("version", order.getVersion())
                .andEqualTo("merchantOrderNo", order.getMerchantOrderNo());
        order.setVersion(order.getVersion() + 1);
        switch (order.getStatus()){
            case 0:
                //未发起支付时只关闭平台
                order.setStatus(TradeStatus.CLOSED.getCode());
                order.setModifyUser("未发起支付平台关闭订单");
                break;
            case 1:
                //平台状态为处理中时去查询支付宝（1.支付宝不存在订单则只关闭平台；2.支付宝存在则先关闭支付宝，然后关闭平台）
                AliWebQueryReq queryReq = new AliWebQueryReq();
                queryReq.setMerchantNo(merchant.getMerchantNo());
                queryReq.setOutTradeNo(req.getOrderNo());
                AlipayTradeQueryResponse queryResponse = alipayHandler.aliWebQueryOrder(queryReq, merchant);
                //支付宝存在订单
                if(queryResponse != null && ALI_SUCCESS_CODE.equals(queryResponse.getCode())){
                    switch (queryResponse.getTradeStatus()){
                        case "TRADE_SUCCESS":
                        case "TRADE_FINISHED":
                            //交易已成功，不能关闭
                            throw new BusinessException("该交易已成功，不能关闭");
                        case "TRADE_CLOSED":
                            throw new BusinessException("该交易已关闭，不能重复关闭");
                        case "WAIT_BUYER_PAY":
                            logger.info("订单{}查询状态为{},开始请求渠道关闭订单", req.getOrderNo(), queryResponse.getTradeStatus());
                            String closeResult = alipayHandler.aliCancelPay(req, merchant);
                            if(closeResult != null){
                                order.setChannelOrderNo(closeResult);
                                order.setStatus(TradeStatus.CLOSED.getCode());
                                order.setModifyUser("关闭支付宝订单");
                            }else {
                                throw new BusinessException("支付宝关闭订单异常！");
                            }
                            break;
                        default:
                            break;
                    }
                }else if(queryResponse != null && ALI_NOT_EXIST_CODE.equals(queryResponse.getSubCode())){
                    //支付宝不存在订单，只关闭平台
                    logger.info("查询支付宝订单号{}渠道返回:{},只关闭平台订单", req.getOrderNo(), queryResponse.getSubMsg());
                    order.setStatus(TradeStatus.CLOSED.getCode());
                    order.setModifyUser("未请求支付宝平台关闭订单");
                }else {
                    throw new BusinessException("支付宝返回:" + queryResponse.getSubMsg());
                }
                break;
            case 2:
                //检查当前交易的状态是不是已经为终态，终态不能发起交易关闭
                throw new BusinessException("该交易已成功，不能关闭");
            case 3:
                throw new BusinessException("该交易已失败，不能关闭");
            case 4:
            case 5:
                throw new BusinessException("该交易已关闭，不能重复关闭");
            default:
                throw new BusinessException("订单状态异常");
        }
        int row = tradePaymentRecordMapper.updateByExampleSelective(order, updateExample);
        if(row != 1){
            throw new BusinessException("关闭订单失败，请重试！");
        }
        return true;
    }


    private TradePaymentRecordDO handlerPaymentRecord(AliF2FPayReq req, MerchantPayInfoDO merchant) throws BusinessException {
        Example example = new Example(TradePaymentRecordDO.class);
        example.createCriteria().andEqualTo("merchantOrderNo", req.getOutTradeNo());
        TradePaymentRecordDO exist = tradePaymentRecordMapper.selectOneByExample(example);
        if(exist != null){
            throw new BusinessException("订单号重复");
        }
        TradePaymentRecordDO record = new TradePaymentRecordDO();
        record.setSysNo(globalSysnoGenerator.nextSysno());
        record.setMerchantNo(merchant.getMerchantNo());
        record.setMerchantName(merchant.getMerchantName());
        record.setMerchantOrderNo(req.getOutTradeNo());
        record.setOrderAmount(req.getTotalAmount());
        //计算手续费(四舍五入) TODO 单笔金额乘以费率表中的费率
        BigDecimal cost = req.getTotalAmount().multiply(new BigDecimal(0));
        record.setMerCost(cost.setScale(2, RoundingMode.HALF_UP));
        record.setPayWayCode(merchant.getPayWayCode());
        record.setPayTypeCode(req.getPayTypeCode());
        record.setProductName(req.getSubject());
        record.setVersion(1);
        record.setStatus(TradeStatus.COMMIT.getCode());
        record.setCreateUser(req.getCreateUser());
        tradePaymentRecordMapper.insertSelective(record);
        return record;
    }
}
