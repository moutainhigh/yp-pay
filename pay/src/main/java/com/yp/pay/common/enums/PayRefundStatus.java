package com.yp.pay.common.enums;

/**
 * 支付单退款单状态
 * 
 * @author: liuX
 * @date: 20200517 15:16
 * @description: TradeStatus
 */
public enum PayRefundStatus implements BaseEnum {

	/**
	 * 未退款
	 */
	REFUND_NOT(0, "未退款"),

	/**
	 * 部分退款
	 */
	REFUND_PART(1, "部分退款"),

	/**
	 * 全额退款
	 */
	REFUND_ALL(2, "全额退款");

	/**
	 * 枚举代码
	 */
	private Integer code;

	/**
	 * 枚举信息
	 */
	private String message;

	PayRefundStatus(Integer code, String message) {
		this.code = code;
		this.message = message;
	}

	public static PayRefundStatus getByCode(Integer code) {
		if (code == null) {
			return null;
		}

		for (PayRefundStatus tradeStatus : values()) {
			if (code.equals(tradeStatus.getCode())) {
				return tradeStatus;
			}
		}
		return null;
	}
}
