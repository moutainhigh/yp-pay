package com.yp.pay.base.exception;

import com.google.common.base.Joiner;
import com.youping.pay.base.entity.StandResponse;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * web异常
 *
 * @author rainbow
 *
 * @date: 2019/5/30
 * @time: 11:10
 * @see [类、类#方法、类#成员]
 */
public class BusinessRuntimeException extends RuntimeException {

	/**
	 *
	 * <变量的意义、目的、功能和可能被用到的地方>
	 *
	 */
	private static final long serialVersionUID = -3626072858605466745L;

	private final int code;

	private Map<String, String> errors;

	public BusinessRuntimeException(String message) {
		super();
		this.code = StandResponse.BUSINESS_EXCEPTION;
		this.errors = Collections.singletonMap(StringUtils.lowerCase(ResultEnum.SERVICE_ERROR.name()), message);
	}

	public BusinessRuntimeException(int code, Map<String, String> errors) {
		super();
		this.code = code;
		this.errors = errors;
	}

	public BusinessRuntimeException(int code, Map<String, String> errors, Throwable e) {
		super(e);
		this.code = code;
		this.errors = errors;
	}

	@Override
	public String toString() {
		return errorToString();
	}

	private String errorToString() {
		if (errors == null) {
			errors = new HashMap<>(0);
		}
		return Joiner.on(",").skipNulls().join(errors.values());
	}

	@Override
	public String getMessage() {
		return errorToString();
	}

	@Override
	public synchronized Throwable fillInStackTrace() {
		return null;
	}

	public int getCode() {
		return code;
	}
}
