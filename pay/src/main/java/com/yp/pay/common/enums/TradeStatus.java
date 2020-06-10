package com.yp.pay.common.enums;

/**
 * 支付交易记录表[trade_payment_record]交易支付状态
 *
 * @author: liuX
 * @date: 20200517 15:16
 * @description: TradeStatus
 */
public enum TradeStatus implements BaseEnum {

	/**
	 * 已提交
	 */
	COMMIT(0, "已提交"),
	/**
	 * 处理中
	 */
	HANDING(1, "处理中"),
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
	CLOSED(4, "交易关闭");

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
