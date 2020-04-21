package com.yp.pay.wx.handler;

import com.yp.pay.wx.config.JWellWXPayConfig;
import com.yp.pay.wx.entity.dao.MerchantPayInfoDO;
import com.yp.pay.wx.mapper.MerchantPayInfoMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WXPayHandler implements InitializingBean {
    @Autowired
    private MerchantPayInfoMapper merchantPayInfoMapper;

    // 存放自定义商户号和对应的数据
    public static Map<String, JWellWXPayConfig> merchantInfoMap;

    // 存放微信商户号和对应的数据
    public static Map<String, JWellWXPayConfig> WXMerIdInfoMap;

    final static Logger LOGGER = LoggerFactory.getLogger(WXPayHandler.class);

    private final static String PAY_TYPE = "WX_PAY";

    public static final Map<String, String> payResultMap = new HashMap<String, String>() {
        {
            /***微信支付订单查询对应的错误码和错误描述信息***/
            put("SUCCESS", "支付成功");
            put("REFUND", "转入退款");
            put("NOTPAY", "未支付");
            put("CLOSED", "已关闭");
            put("REVOKED", "已撤销");
            put("USERPAYING", "用户支付中");
            put("PAYERROR", "支付失败(其他原因)");
        }
    };

    @Override
    public void afterPropertiesSet() {

        Map<String, Object> data = new HashMap<>();
        data.put("status", 1);
        data.put("payWayCode", PAY_TYPE);

        // 获取微信渠道所有数据
        List<MerchantPayInfoDO> merchantPayInfoDOS = merchantPayInfoMapper.selectMerchantInfo(data);
        if (merchantPayInfoDOS == null || merchantPayInfoDOS.size() == 0) {
            LOGGER.error("当前没有配置微信交易渠道信息或者微信交易渠道被冻结，请核微信支付实渠道信息。");
        }

        merchantInfoMap = new HashMap<>();
        WXMerIdInfoMap = new HashMap<>();
        for (MerchantPayInfoDO merchantPayInfoDO : merchantPayInfoDOS) {

            String merchantNo = merchantPayInfoDO.getMerchantNo();
            if (StringUtils.isBlank(merchantNo)) {
                LOGGER.error("数据库微信支付渠道记录号为[" + merchantPayInfoDO.getSysno() + "]道配置有误，" +
                        "该渠道信息没有配置平台商户号merchantNo");
                continue;
            }

            // 微信渠道商户数据校验以及初始化
            if (PAY_TYPE.equals(merchantPayInfoDO.getPayWayCode())) {

                String appId = merchantPayInfoDO.getAppId();
                if (StringUtils.isBlank(appId)) {
                    LOGGER.error("微信支付渠道商户号[" + merchantNo + "]的微信交易渠道没有配置appid信息。");
                    continue;
                }

                String mchId = merchantPayInfoDO.getSubMerchantId();
                if (StringUtils.isBlank(mchId)) {
                    LOGGER.error("微信支付渠道商户号[" + merchantNo + "]的微信交易渠道没有配置微信商户号(mch_id)信息。");
                    continue;
                }

                String secretKey = merchantPayInfoDO.getPartnerKey();
                if (StringUtils.isBlank(secretKey)) {
                    LOGGER.error("微信支付渠道商户号[" + merchantNo + "]的微信交易渠道没有配置秘钥(API秘钥)信息。");
                    continue;
                }

                // 通过对应的商户号获取当前商户的证书路径
                String path = merchantPayInfoDO.getCertPath();
                LOGGER.info("证书路径：" + path);
                // 通过商户路径初始化证书信息
                File file = new File(path);
                InputStream certStream;
                byte[] certData = new byte[(int) file.length()];
                try {
                    certStream = new FileInputStream(file);
                    certStream.read(certData);
                    certStream.close();
                } catch (Exception e) {
                    LOGGER.error("微信支付渠道商户号[" + merchantNo + "]的微信的证书初始化异常。");
                    LOGGER.info("初始化>>>>>>>>>>>>微信支付渠道证书信息初始化失败！");
                }

                JWellWXPayConfig jWellWXPayConfig = new JWellWXPayConfig(certData, appId, mchId, secretKey, merchantPayInfoDO);
                merchantInfoMap.put(merchantNo, jWellWXPayConfig);
                WXMerIdInfoMap.put(mchId, jWellWXPayConfig);
            }

        }

        LOGGER.info("初始化>>>>>>>>>>>>微信支付渠道配置信息完成");
    }

}
