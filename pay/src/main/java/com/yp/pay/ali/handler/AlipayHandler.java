package com.yp.pay.ali.handler;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePayRequestBuilder;
import com.alipay.demo.trade.model.builder.AlipayTradeQueryRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPayResult;
import com.alipay.demo.trade.model.result.AlipayF2FQueryResult;
import com.alipay.demo.trade.service.AlipayMonitorService;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayMonitorServiceImpl;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.service.impl.AlipayTradeWithHBServiceImpl;
import com.yp.pay.ali.config.AlipayConfiguration;
import com.yp.pay.base.exception.BusinessException;
import com.yp.pay.common.util.StringUtil;
import com.yp.pay.entity.aliandwx.entity.MerchantPayInfoDO;
import com.yp.pay.entity.aliandwx.req.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.RoundingMode;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: lijiang
 * @date: 2019.12.11 15:53
 * @description: AlipayHandler
 */
@Service
public class AlipayHandler {

    private static final Logger log = LoggerFactory.getLogger(AlipayHandler.class);

    private AlipayTradeService tradeService;

    private AlipayTradeService tradeWithHBService;

    private AlipayMonitorService monitorService;

    @Autowired
    private AlipayConfiguration config;

    public void initConfigs(MerchantPayInfoDO payInfo) throws BusinessException {

        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的alipayrisk10.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");
        Configs.setAppid(payInfo.getAppId());
        Configs.setPid(payInfo.getPartnerId());
        Configs.setPrivateKey(payInfo.getRsaPrivateKey());
        Configs.setPublicKey(payInfo.getRsaPublicKey());
        Configs.setAlipayPublicKey(payInfo.getAliPublicKey());

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();

        /** 支付宝当面付2.0服务（集成了交易保障接口逻辑）**/
        tradeWithHBService = new AlipayTradeWithHBServiceImpl.ClientBuilder().build();

        /** 如果需要在程序中覆盖Configs提供的默认参数, 可以使用ClientBuilder类的setXXX方法修改默认参数 否则使用代码中的默认设置 */
        monitorService = new AlipayMonitorServiceImpl.ClientBuilder()
                .setCharset("GBK")
                .setFormat("json")
                .build();
    }

    public AlipayF2FPayResult aliF2FPay(AliF2FPayReq req, MerchantPayInfoDO merchant) {

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId(merchant.getPartnerId());

        // 支付超时，线下扫码交易定义为5分钟
        if (StringUtils.isEmpty(req.getTimeoutExpress())) {
            req.setTimeoutExpress("5m");
        }

        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();
        if (!CollectionUtils.isEmpty(req.getPlatGoods())) {
            // 商品明细列表，需填写购买商品详细信息，
            req.getPlatGoods().forEach(good -> {

                // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
                GoodsDetail goods1 = GoodsDetail.newInstance(good.getSkuId(), good.getSkuName(), good.getSinglePrice(), good.getNums());
                // 创建好一个商品后添加至商品明细列表
                goodsDetailList.add(goods1);
            });
        }

        // 创建条码支付请求builder，设置请求参数
        AlipayTradePayRequestBuilder builder = new AlipayTradePayRequestBuilder()
                .setOutTradeNo(req.getOutTradeNo()).setSubject(req.getSubject()).setAuthCode(req.getAuthCode())
                .setTotalAmount(req.getTotalAmount().toString())
                .setStoreId(req.getStoreId())
                .setUndiscountableAmount(req.getUndiscountableAmount().toString()).setBody(req.getBody())
                .setExtendParams(extendParams)
                .setGoodsDetailList(goodsDetailList).setTimeoutExpress(req.getTimeoutExpress());
        // 调用tradePay方法获取当面付应答
        AlipayF2FPayResult result = tradeWithHBService.tradePay(builder);
        log.info("当面付返回：" + result.getResponse().toString());
        return result;
    }


    public String aliWebPay(AliWebPayReq req, MerchantPayInfoDO merchant){

        // SDK 公共请求类，包含公共请求参数，以及封装了签名与验签，开发者无需关注签名与验签
        //调用RSA签名方式
        AlipayClient client = new DefaultAlipayClient(config.getUrl(), merchant.getAppId(), merchant.getRsaPrivateKey(),
                config.getFormat(), config.getCharset(), merchant.getRsaPublicKey(), config.getSignType());
        AlipayTradeWapPayRequest alipay_request=new AlipayTradeWapPayRequest();
        // 封装请求支付信息
        AlipayTradeWapPayModel model=new AlipayTradeWapPayModel();
        model.setOutTradeNo(req.getOutTradeNo());
        model.setSubject(req.getSubject());
        model.setTotalAmount(String.valueOf(req.getTotalAmount()));
        model.setBody(req.getBody());
        model.setTimeoutExpress(req.getTimeoutExpress());
        model.setProductCode(req.getProductCode());
        model.setQuitUrl(merchant.getQuitUrl());
        if(!StringUtils.isEmpty(req.getAttach())){
            model.setPassbackParams(URLEncoder.encode(req.getAttach()));
        }
        alipay_request.setBizModel(model);
        // 设置异步通知地址
        alipay_request.setNotifyUrl(merchant.getNotifyUrl());
        // 设置支付成功返回地址，拼接页面展示参数
        StringBuilder builder = new StringBuilder(merchant.getReturnUrl());
        builder.append("?merchantName=").append(merchant.getMerchantName())
                .append("&type=alipay")
                .append("&merchantNo=").append(merchant.getMerchantNo())
                .append("&orderNo=").append(req.getOutTradeNo())
                .append("&productName=").append(req.getSubject())
                .append("&orderAmount=").append(req.getTotalAmount().toString());
        alipay_request.setReturnUrl(builder.toString());
        // form表单生产
        String form = "";
        try {
            // 调用SDK生成表单
            form = client.pageExecute(alipay_request).getBody();
            System.out.println("支付接口返回表单：" + form);
            return form;
        } catch (AlipayApiException e) {
            e.printStackTrace();
            log.info("手机网站支付接口异常！请检查");
        }
        return form;
    }

    public void aliWebRefund(AliWebRefundReq req, MerchantPayInfoDO merchant){
        // SDK 公共请求类，包含公共请求参数，以及封装了签名与验签，开发者无需关注签名与验签
        AlipayClient client = new DefaultAlipayClient(config.getUrl(), merchant.getAppId(), merchant.getRsaPrivateKey(),
                config.getFormat(), config.getCharset(), merchant.getAliPublicKey(), config.getSignType());
        AlipayTradeRefundRequest alipay_request = new AlipayTradeRefundRequest();

        AlipayTradeRefundModel model=new AlipayTradeRefundModel();
        model.setOutTradeNo(req.getOutTradeNo());
        model.setTradeNo("gatewayPayRefund" + StringUtil.getDate("yyyyMMddHHmmss"));
        model.setRefundAmount(req.getRefundAmount().toString());
        model.setRefundReason(req.getRefundReason());
        model.setOutRequestNo(req.getOutRequestNo());
        alipay_request.setBizModel(model);
        AlipayTradeRefundResponse alipay_response = null;
        try {
            alipay_response = client.execute(alipay_request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        System.out.println(alipay_response.getBody());
    }

    public AlipayTradeQueryResponse aliWebQueryOrder(AliWebQueryReq req, MerchantPayInfoDO merchant){
        // SDK 公共请求类，包含公共请求参数，以及封装了签名与验签，开发者无需关注签名与验签
        AlipayClient client = new DefaultAlipayClient(config.getUrl(), merchant.getAppId(), merchant.getRsaPrivateKey(),
                config.getFormat(), config.getCharset(), merchant.getAliPublicKey(), config.getSignType());

        AlipayTradeQueryRequest alipayRequest = new AlipayTradeQueryRequest();

        AlipayTradeQueryModel model=new AlipayTradeQueryModel();
        model.setOutTradeNo(req.getOutTradeNo());
        alipayRequest.setBizModel(model);

        AlipayTradeQueryResponse alipayResponse = null;
        try {
            alipayResponse = client.execute(alipayRequest);
            log.info("查询支付宝订单结果为{}", alipayResponse.toString());
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return alipayResponse;
    }


    public AlipayF2FQueryResult queryAliF2FResult(AlipayTradeQueryRequestBuilder queryBuiler) {
        AlipayF2FQueryResult result = tradeWithHBService.queryTradeResult(queryBuiler);
        return result;
    }

    public String aliCancelPay(AliCancelPayReq req, MerchantPayInfoDO merchant) throws BusinessException {
        // SDK 公共请求类，包含公共请求参数，以及封装了签名与验签，开发者无需关注签名与验签
        AlipayClient client = new DefaultAlipayClient(config.getUrl(), merchant.getAppId(), merchant.getRsaPrivateKey(),
                config.getFormat(), config.getCharset(), merchant.getAliPublicKey(), config.getSignType());

        AlipayTradeCloseRequest aliPayClose = new AlipayTradeCloseRequest();

        AlipayTradeCloseModel model = new AlipayTradeCloseModel();
        model.setOutTradeNo(req.getOrderNo());
        aliPayClose.setNotifyUrl(merchant.getNotifyUrl());
        aliPayClose.setBizModel(model);
        try {
            AlipayTradeCloseResponse alipayResponse = client.execute(aliPayClose);
            log.info("取消支付宝订单结果为{}", JSONObject.toJSONString(alipayResponse));
            if("10000".equals(alipayResponse.getCode())){
                return alipayResponse.getTradeNo();
            }else {
                throw new BusinessException("关闭订单支付宝返回：" + alipayResponse.getSubMsg());
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Boolean aliRefundPay(AliRefundPayReq req, MerchantPayInfoDO merchant) throws BusinessException {
        // SDK 公共请求类，包含公共请求参数，以及封装了签名与验签，开发者无需关注签名与验签
        AlipayClient client = new DefaultAlipayClient(config.getUrl(), merchant.getAppId(), merchant.getRsaPrivateKey(),
                config.getFormat(), config.getCharset(), merchant.getAliPublicKey(), config.getSignType());

        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();

        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutTradeNo(req.getOrderNo());
        model.setRefundAmount(req.getRefundAmount().toString());
        model.setRefundReason(req.getRefundReason());
        model.setOutRequestNo(req.getRefundOrderNo());
        request.setNotifyUrl(merchant.getNotifyUrl());
        request.setBizModel(model);
        try {
            AlipayTradeRefundResponse alipayResponse = client.execute(request);
            log.info("支付宝订单退款结果为{}", JSONObject.toJSONString(alipayResponse));
            if("10000".equals(alipayResponse.getCode())){
                /**
                 * "code": "10000",
                 *         "msg": "Success",
                 *         "trade_no": "支付宝交易号",
                 *         "out_trade_no": "6823789339978248",
                 *         "buyer_logon_id": "159****5620",
                 *         "fund_change": "Y",
                 *         "refund_fee": 88.88,
                 *         "refund_currency": "USD",
                 *         "gmt_refund_pay": "2014-11-27 15:45:57",
                 *         "refund_detail_item_list": [
                 *             {
                 *                 "fund_channel": "ALIPAYACCOUNT",
                 *                 "bank_code": "CEB",
                 *                 "amount": 10,
                 *                 "real_amount": 11.21,
                 *                 "fund_type": "DEBIT_CARD"
                 *             }
                 *         ],
                 *         "store_name": "望湘园联洋店",
                 *         "buyer_user_id": "2088101117955611",
                 *         "refund_preset_paytool_list": {
                 *             "amount": [
                 *                 12.21
                 *             ],
                 *             "assert_type_code": "盒马礼品卡:HEMA；抓猫猫红包:T_CAT_COUPON"
                 *         },
                 *         "refund_settlement_id": "2018101610032004620239146945",
                 *         "present_refund_buyer_amount": "88.88",
                 *         "present_refund_discount_amount": "88.88",
                 *         "present_refund_mdiscount_amount": "88.88"
                 *     },
                 *     "sign": "ERITJKEIJKJHKKKKKKKHJEREEEEEEEEEEE"
                 */
            }else {
                throw new BusinessException("订单退款支付宝返回：" + alipayResponse.getSubMsg());
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
            return null;
        }
        return true;
    }
}
