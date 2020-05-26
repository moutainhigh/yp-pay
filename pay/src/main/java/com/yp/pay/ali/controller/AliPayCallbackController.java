package com.yp.pay.ali.controller;

import com.yp.pay.ali.service.AliPayCallbackService;
import com.yp.pay.base.controller.BaseController;
import com.yp.pay.base.entity.StandResponse;
import com.yp.pay.base.exception.BusinessException;
import com.yp.pay.entity.dto.AliCallBackInfoDTO;
import com.yp.pay.entity.dto.AliRefundCallBackInfoDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 支付宝支付回调
 *
 * @author lijiang
 * @date 20200225
 */
@RequestMapping("v1/aliPayCallBack")
@RestController
@Validated
@Api(value = "支付宝支付回调通知", produces = "application/json;charset=UTF-8")
public class AliPayCallbackController extends BaseController {

	@Autowired
	AliPayCallbackService aliPayCallbackService;

	@ApiOperation("支付宝网站支付异步通知结果处理")
	@PostMapping("/webPayNotify")
	public StandResponse<AliCallBackInfoDTO> webPayNotify(@RequestBody Map<String, String> params) throws BusinessException {

		// 接收银行异步返回数据并将数据更新到数据库中
		AliCallBackInfoDTO callBackInfoDTO = aliPayCallbackService.dealAliPayCallBackData(params);

		return success(callBackInfoDTO);
	}

	@ApiOperation("支付宝扫码支付退款异步通知结果处理")
	@PostMapping("/refundNotify")
	public StandResponse<AliRefundCallBackInfoDTO> refundNotify(Map<String, String> params) throws BusinessException {

		// 接收银行异步返回数据并将数据更新到数据库中
		AliRefundCallBackInfoDTO refundCallBackInfoDTO = aliPayCallbackService.dealAliRefundBackData(params);
		return success(refundCallBackInfoDTO);
	}

}
