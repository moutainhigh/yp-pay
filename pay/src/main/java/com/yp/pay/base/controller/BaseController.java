package com.yp.pay.base.controller;

import com.yp.pay.base.entity.StandResponse;

/**
 * 基础的controller
 * 
 * @author rainbow
 * 
 * @date: 2020/3/30
 * @time: 10:53
 * @see [类、类#方法、类#成员]
 */
public abstract class BaseController {

	protected <E> StandResponse<E> success() {
		return StandResponseBuilder.ok();
	}

	protected <E> StandResponse<E> success(E data) {
		return StandResponseBuilder.ok(data);
	}

	protected <E> StandResponse<E> fail() {
		return StandResponseBuilder.result(StandResponse.INTERNAL_SERVER_ERROR, "系统错误");
	}

	protected <E> StandResponse<E> fail(Integer code, String message) {
		return StandResponseBuilder.result(code, message);
	}
//
//	/**
//	 * 获取用户信息
//	 *
//	 * @param: []
//	 * @return: java.lang.String
//	 * @date: 2020/4/3
//	 * @time: 13:04
//	 * @see [类、类#方法、类#成员]
//	 */
//	protected String getPrincipal() {
//		Authentication authentication = getAuthentication();
//		OAuth2Authentication oAuth2Authentication = (OAuth2Authentication) authentication;
//		if (oAuth2Authentication.isClientOnly()) {
//			throw new BusinessRuntimeException("token类型错误不支持用户信息获取");
//		}
//		return (String) oAuth2Authentication.getPrincipal();
//	}
//
//	/**
//	 * 获取当前登录应用appId
//	 *
//	 * @param: []
//	 * @return: java.lang.String
//	 * @date: 2020/4/3
//	 * @time: 13:04
//	 * @see [类、类#方法、类#成员]
//	 */
//	protected String getAuthenticationClientId() {
//		Authentication authentication = getAuthentication();
//		OAuth2Authentication oAuth2Authentication = (OAuth2Authentication) authentication;
//		return oAuth2Authentication.getOAuth2Request().getClientId();
//	}
//
//	protected Authentication getAuthentication() {
//		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//		if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
//			throw new BusinessRuntimeException("请传入token信息");
//		}
//		return authentication;
//	}

}
