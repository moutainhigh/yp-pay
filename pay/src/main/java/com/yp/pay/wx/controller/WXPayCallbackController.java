package com.yp.pay.wx.controller;

import com.yp.pay.base.controller.BaseController;
import com.yp.pay.base.entity.StandResponse;
import com.yp.pay.base.exception.BusinessException;
import com.yp.pay.entity.aliandwx.dto.CallBackInfoDTO;
import com.yp.pay.entity.aliandwx.dto.RefundCallBackInfoDTO;
import com.yp.pay.wx.service.WXPayCallbackService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 微信支付回调
 *
 * @author liuX
 * @date 20200206
 */
@RequestMapping("v1/wxPayCallBack")
@RestController
@Validated
@Api(value = "微信支付回调通知", produces = "application/json;charset=UTF-8")
public class WXPayCallbackController extends BaseController {

	@Autowired
	WXPayCallbackService wxPayCallbackService;

	@ApiOperation("微信扫码支付异步通知结果处理")
	@PostMapping("/scanPayNotify")
	public StandResponse<CallBackInfoDTO> scanPayNotify(String xmlData) throws BusinessException {

		// 接收银行异步返回数据并将数据更新到数据库中
		CallBackInfoDTO callBackInfoDTO = wxPayCallbackService.dealWXPayCallBackData(xmlData);

		return success(callBackInfoDTO);
	}

	@ApiOperation("微信扫码支付退款异步通知结果处理")
	@PostMapping("/refundNotify")
	public StandResponse<RefundCallBackInfoDTO> refundNotify(String xmlData) throws BusinessException {

		// 接收银行异步返回数据并将数据更新到数据库中
		RefundCallBackInfoDTO refundCallBackInfoDTO = wxPayCallbackService.dealWXRefundBackData(xmlData);

		return success(refundCallBackInfoDTO);
	}

}
