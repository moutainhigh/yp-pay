package com.yp.pay.common.enums;

/**
 * @author: tangxinjun
 * @date: 2019/3/18 15:16
 * @description: TradeStatus
 */
public enum TradeStatus implements BaseEnum {

	/**
	 * 已创建---只针对充值类型为网关充值有效
	 */
	CREATE(0, "已创建"),

	/**
	 * 已提交
	 */
	COMMIT(0, "已提交"),
	/**
	 * 处理中
	 */
	HADING(1, "处理中"),
	/**
	 * 交易成功
	 */
	SUCCESS(2, "交易成功"),
	/**
	 * 交易失败
	 */
	FAIL(3, "交易失败"),
	/**
	 * 交易关闭
	 */
	CLOSE(4, "交易关闭"),
	/**
	 * 平台关闭
	 */
	PLAT_CLOSE(5, "平台关闭");

	/**
	 * 枚举代码
	 */
	private Integer code;

	/**
	 * 枚举信息
	 */
	private String message;

	TradeStatus(Integer code, String message) {
		this.code = code;
		this.message = message;
	}

	public static TradeStatus getByCode(Integer code) {
		if (code == null) {
			return null;
		}

		for (TradeStatus tradeStatus : values()) {
			if (code.equals(tradeStatus.getCode())) {
				return tradeStatus;
			}
		}
		return null;
	}
}
