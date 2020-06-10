package com.yp.pay.common.enums;

/**
 * 退款记录表[trade_refund_record]中退款状态
 *
 * @author: liuX
 * @date: 20200517 15:16
 * @description: TradeStatus
 */
public enum RefundStatus implements BaseEnum {

	/**
	 * 已申请
	 */
	REFUND_APPLY(0, "已申请"),

	/**
	 * 退款中
	 */
	REFUNDING(1, "退款中"),

	/**
	 * 交易成功
	 */
	REFUND_SUCCESS(2, "退款成功"),

	/**
	 * 交易失败
	 */
	REFUND_FAIL(3, "退款失败");

	/**
	 * 枚举代码
	 */
	private Integer code;

	/**
	 * 枚举信息
	 */
	private String message;

	RefundStatus(Integer code, String message) {
		this.code = code;
		this.message = message;
	}

	public static RefundStatus getByCode(Integer code) {
		if (code == null) {
			return null;
		}

		for (RefundStatus tradeStatus : values()) {
			if (code.equals(tradeStatus.getCode())) {
				return tradeStatus;
			}
		}
		return null;
	}
}
