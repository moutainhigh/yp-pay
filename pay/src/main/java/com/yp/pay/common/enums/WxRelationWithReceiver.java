package com.yp.pay.common.enums;

/**
 * 与分账方的关系类型
 *
 * @author: liuX
 * @date: 20200517 15:16
 * @description: TradeStatus
 */
public enum WxRelationWithReceiver implements BaseIIEnum {

	/**
	 * 服务商
	 */
	SERVICE_PROVIDER(0, "SERVICE_PROVIDER","服务商"),

	/**
	 * 门店
	 */
	STORE(1, "STORE","门店"),

	/**
	 * 员工
	 */
	STAFF(2, "STAFF","员工"),

	/**
	 * 店主
	 */
	STORE_OWNER(3, "STORE_OWNER","店主"),

	/**
	 * 合作伙伴
	 */
	PARTNER(4, "PARTNER","合作伙伴"),

	/**
	 * 总部
	 */
	HEADQUARTER(5, "HEADQUARTER","总部"),

	/**
	 * 品牌方
	 */
	BRAND(6, "BRAND","品牌方"),

	/**
	 * 分销商
	 */
	DISTRIBUTOR(7, "DISTRIBUTOR","分销商"),

	/**
	 * 用户
	 */
	USER(8, "USER","用户"),

	/**
	 * 供应商
	 */
	SUPPLIER(9, "SUPPLIER","供应商"),

	/**
	 * 自定义
	 */
	CUSTOM(10, "CUSTOM","自定义");

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

	WxRelationWithReceiver(Integer code, String value, String message) {
		this.code = code;
		this.value = value;
		this.message = message;
	}

	public static WxRelationWithReceiver getByCode(Integer code) {
		if (code == null) {
			return null;
		}

		for (WxRelationWithReceiver tradeStatus : values()) {
			if (code.equals(tradeStatus.getCode())) {
				return tradeStatus;
			}
		}
		return null;
	}
}
