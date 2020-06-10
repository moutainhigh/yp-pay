package com.yp.pay.common.enums;

/**
 * 分账详情记录表[profit_share_detail]中分账状态
 *
 * @author: liuX
 * @date: 20200517 15:16
 * @description: ShareDetailStatus
 */
public enum ShareDetailStatus implements BaseEnum {

	/**
	 * 处理中
	 */
	SHARE_HANDING(0, "处理中"),

	/**
	 * 分账成功
	 */
	SHARE_SUCCESS(1, "分账成功"),

	/**
	 * 分账失败
	 */
	SHARE_FAIL(2, "分账失败"),

	/**
	 * 分账回退
	 */
	SHARE_REFUND(3, "分账回退");


	/**
	 * 枚举代码
	 */
	private Integer code;

	/**
	 * 枚举信息
	 */
	private String message;

	ShareDetailStatus(Integer code, String message) {
		this.code = code;
		this.message = message;
	}

	public static ShareDetailStatus getByCode(Integer code) {
		if (code == null) {
			return null;
		}

		for (ShareDetailStatus tradeStatus : values()) {
			if (code.equals(tradeStatus.getCode())) {
				return tradeStatus;
			}
		}
		return null;
	}
}
