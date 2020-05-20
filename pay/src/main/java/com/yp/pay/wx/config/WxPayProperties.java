package com.yp.pay.wx.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "wxpay")
public class WxPayProperties {

	/**
	 * 生成二维码后返回前端页面的地址
	 */
	private String qrCodeReturnUrl;

	/**
	 * 微信获取openID的请求地址
	 */
	private String openIdUrl;

}
