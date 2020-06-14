package com.yp.pay.wx.config;

import com.github.wxpay.sdk.IWXPayDomain;
import com.github.wxpay.sdk.WXPayConfig;
import com.yp.pay.entity.entity.MerchantPayInfoDO;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @description: 商户配置信息
 *
 * @author: liuX
 * @time: 2020/5/29 14:17
 */
public class WxPayConfig extends WXPayConfig {

    public byte[] certData;

    public String appId;

    public String mchId;

    public String secretKey;

    public MerchantPayInfoDO merchantPayInfoDO;

    public WxPayConfig(byte[] certData, String appId, String mchId, String secretKey, MerchantPayInfoDO merchantPayInfoDO) {
        this.certData = certData;
        this.appId = appId;
        this.mchId = mchId;
        this.secretKey = secretKey;
        this.merchantPayInfoDO = merchantPayInfoDO;
    }

    /**
     * 获取 App ID
     *
     * @return App ID
     */
    @Override
    public String getAppID() {
        return this.appId;
    }

    /**
     * 获取 Mch ID
     *
     * @return Mch ID
     */
    @Override
    public String getMchID() {
        return this.mchId;
    }

    /**
     * 获取 API 密钥
     *
     * @return API密钥
     */
    @Override
    public String getKey() {
        return this.secretKey;
    }

    /**
     * 获取商户证书内容
     *
     * @return 商户证书内容
     */
    @Override
    public InputStream getCertStream() {

        ByteArrayInputStream certBis = new ByteArrayInputStream(this.certData);

        return certBis;
    }

    /**
     * 获取渠道配置详细信息
     * @return MerchantPayInfoDO 渠道配置详情对象
     */
    public MerchantPayInfoDO getMerchantPayInfoDO(){
        return this.merchantPayInfoDO;
    }

    /**
     * 获取WXPayDomain, 用于多域名容灾自动切换
     *
     * @return
     */
    @Override
    public IWXPayDomain getWXPayDomain() {

        IWXPayDomain iwxPayDomain = new IWXPayDomain() {

            @Override
            public void report(String domain, long elapsedTimeMillis, Exception ex) {

            }

            @Override
            public DomainInfo getDomain(WXPayConfig config) {
                return new IWXPayDomain.DomainInfo("api.mch.weixin.qq.com", true);
            }

        };

        return iwxPayDomain;
    }

}
