package com.yp.pay.base.exception;

/**
 * 响应消息类型
 * 
 * @author rainbow
 * 
 * @date: 2020/3/30
 * @time: 12:51
 * @see [类、类#方法、类#成员]
 */
public enum ResultEnum {

	SUCCESS(0, "success"),

	CONTROLLER_ERROR(1001, "controller error"),

	SERVICE_ERROR(1002, "service error"),

	DOMAIN_ERROR(1003, "domain error"),
	//
	DAO_ERROR(1004, "dao error"),
	//
	API_ERROR(1005, "api error"),
	//
	CLIENT_ERROR(1006, "client error"),

	SYSTEM_ERROR(1007, "server error"),

	SIGN_ERROR(1008, "sign error"),

	PARAM_DATA_ERROR(1009, "param error"),
	// 1006 没有权限
	UNAUTHORIZED_ERROR(1010, "unauthorized"),
	// session超时
	SESSION_TIMEOUT(10011, "session time out"),
	// 退出code
	LOGOUT_ERROR(10012, "logout"),

	// 未知异常
	UNKNOW_ERROR(-99, "system error");

	private Integer code;

	private String value;

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * 获取异常内容
	 *
	 * @return
	 */
	public String getValue() {
		return value;
	}

	private ResultEnum(Integer code, String value) {
		this.code = code;
		this.value = value;
	}

	/**
	 * TODO 方法描述
	 *
	 * @param: [code]
	 * @return: ResultEnum
	 * @date: 2017/11/3
	 * @time: 上午10:13
	 * @see [类、类#方法、类#成员]
	 */
	public static ResultEnum getResultType(Integer code) {
		ResultEnum[] errors = ResultEnum.values();
		for (ResultEnum error : errors) {
			if (error.getCode().equals(code)) {
				return error;
			}
		}
		return UNKNOW_ERROR;
	}

	public static boolean success(Integer result) {
		return SUCCESS.code.equals(result);
	}
}
