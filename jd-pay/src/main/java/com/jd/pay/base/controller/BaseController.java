package com.jd.pay.base.controller;

import com.jd.pay.base.entity.StandResponse;

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

}
