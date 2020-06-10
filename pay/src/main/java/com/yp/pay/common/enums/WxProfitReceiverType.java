package com.yp.pay.common.enums;

/**
 * 分账接收方类型
 *
 * @author: liuX
 * @date: 20200517 15:16
 * @description: WxProfitReceiverType
 */
public enum WxProfitReceiverType implements BaseIIEnum {

	/**
	 * 商户ID
	 */
	MERCHANT_ID(1, "MERCHANT_ID","商户ID"),

	/**
	 * 个人微信号
	 */
	PERSONAL_WECHATID(2, "PERSONAL_WECHATID","个人微信号"),

	/**
	 * 个人openid
	 */
	PERSONAL_OPENID(3, "PERSONAL_OPENID","个人openid");

	/**
	 * 枚举代码
	 */
	private Integer code;

	/**
	 * 枚举值
	 */
	private String value;

	/**
	 * 枚举值描述信息
	 */
	private String message;

	WxProfitReceiverType(Integer code, String value, String message) {
		this.code = code;
		this.value = value;
		this.message = message;
	}

	public static WxProfitReceiverType getByCode(Integer code) {
		if (code == null) {
			return null;
		}

		for (WxProfitReceiverType tradeStatus : values()) {
			if (code.equals(tradeStatus.getCode())) {
				return tradeStatus;
			}
		}
		return null;
	}
}
