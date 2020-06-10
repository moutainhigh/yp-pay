package com.yp.pay.common.enums;

/**
 * 分账记录表[profit_share_record]中分账回退状态
 *
 * @author: liuX
 * @date: 20200517 15:16
 * @description: ShareRefundStatus
 */
public enum ShareRefundStatus implements BaseEnum {

	/**
	 * 未回退
	 */
	SHARE_UN_REFUND(0, "未回退"),

	/**
	 * 回退
	 */
	SHARE_REFUND(1, "回退");

	/**
	 * 枚举代码
	 */
	private Integer code;

	/**
	 * 枚举信息
	 */
	private String message;

	ShareRefundStatus(Integer code, String message) {
		this.code = code;
		this.message = message;
	}

	public static ShareRefundStatus getByCode(Integer code) {
		if (code == null) {
			return null;
		}

		for (ShareRefundStatus tradeStatus : values()) {
			if (code.equals(tradeStatus.getCode())) {
				return tradeStatus;
			}
		}
		return null;
	}
}
