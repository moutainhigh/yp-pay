package com.yp.pay.ali.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author: lijiang
 * @date: 2019.12.11 16:25
 * @description: AlipayConfiguration
 */
@Configuration
@PropertySource("classpath:zfbinfo.properties")
@Data
public class AlipayConfiguration {

    @Value("${appid}")
    private String appId;

    @Value("${open_api_domain}")
    private String url;

    @Value("${public_key}")
    private String publicKey;

    @Value("${private_key}")
    private String privateKey;

    @Value("${sign_type}")
    private String signType;

    @Value("${charset}")
    private String charset;

    @Value("${format}")
    private String format;

    @Value("${alipay_public_key}")
    private String alipayPublicKey;

}
