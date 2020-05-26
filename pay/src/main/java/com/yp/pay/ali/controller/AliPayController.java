package com.yp.pay.ali.controller;

import com.yp.pay.ali.service.AliPayService;
import com.yp.pay.base.controller.BaseController;
import com.yp.pay.base.entity.StandResponse;
import com.yp.pay.base.exception.BusinessException;
import com.yp.pay.entity.dto.MerchantInfoDTO;
import com.yp.pay.entity.dto.TradePaymentRecordDTO;
import com.yp.pay.entity.req.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author: lijiang
 * @date: 2019.12.10 17:30
 * @description: AliPayController
 */
@RestController
@RequestMapping("/v1/alipay/")
@Validated
@Api(value = "支付宝支付接口")
public class AliPayController extends BaseController {

    @Autowired
    private AliPayService alipayService;

    private static final Logger log = LoggerFactory.getLogger(AliPayController.class);
    @RequestMapping(value = "/scanningPay", method = RequestMethod.POST)
    @ApiOperation(value = "支付宝扫码支付")
    public StandResponse<String> scanningPay(@RequestBody @Valid AliF2FPayReq req) throws BusinessException {
        return success(alipayService.scanningPay(req));
    }

    @RequestMapping(value = "/webPay", method = RequestMethod.POST)
    @ApiOperation(value = "支付宝手机网站支付")
    public StandResponse<String> webPay(@RequestBody @Valid AliWebPayReq req) throws BusinessException {
        return success(alipayService.webPay(req));
    }

    @RequestMapping(value = "/queryMerchantByPayCode", method = RequestMethod.POST)
    @ApiOperation(value = "通过商户号和支付方式获取商户")
    public StandResponse<MerchantInfoDTO> queryMerchantByPayCode(@RequestBody @Valid MerchantQueryReq req) throws BusinessException {
        return success(alipayService.queryMerchantByPayCode(req));
    }

    @RequestMapping(value = "/webPayQuery", method = RequestMethod.POST)
    @ApiOperation(value = "支付宝手机网站支付订单查询")
    public StandResponse<TradePaymentRecordDTO> webPayQuery(@RequestBody @Valid AliWebQueryReq req) throws BusinessException {
        return success(alipayService.webPayQuery(req));
    }

    @RequestMapping(value = "/getScanningPayResult", method = RequestMethod.POST)
    @ApiOperation(value = "支付宝扫码支付结果查询")
    public StandResponse<TradePaymentRecordDTO> getScanningPayPlatResult(@RequestBody @Valid AliOrderQueryReq aliOrderQueryReq) throws BusinessException {
        return success(alipayService.getScanningPayPlatResult(aliOrderQueryReq));
    }

    @RequestMapping(value = "/queryScanningPayResult", method = RequestMethod.GET)
    public StandResponse queryScanningPayResult(){
        alipayService.queryScanningPayResultJob();
        return success();
    }

    @RequestMapping(value = "/closePay", method = RequestMethod.POST)
    @ApiOperation(value = "支付宝关闭订单")
    public StandResponse<Boolean> aliCancelPay(@RequestBody @Valid AliCancelPayReq req) throws BusinessException {
        return success(alipayService.aliCancelPay(req));
    }

}
