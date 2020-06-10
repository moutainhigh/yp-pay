package com.yp.pay.common.enums;

/**
 * 分账接收方数据[profit_share_receiver]中分账方状态
 *
 * @author: liuX
 * @date: 20200517 15:16
 * @description: ReceiverStatus
 */
public enum ReceiverStatus implements BaseEnum {

	/**
	 * 添加成功
	 */
	ADD_SUCCESS(0, "添加成功"),

	/**
	 * 已删除
	 */
	REMOVED(1, "已删除");

	/**
	 * 枚举代码
	 */
	private Integer code;

	/**
	 * 枚举信息
	 */
	private String message;

	ReceiverStatus(Integer code, String message) {
		this.code = code;
		this.message = message;
	}

	public static ReceiverStatus getByCode(Integer code) {
		if (code == null) {
			return null;
		}

		for (ReceiverStatus tradeStatus : values()) {
			if (code.equals(tradeStatus.getCode())) {
				return tradeStatus;
			}
		}
		return null;
	}
}
