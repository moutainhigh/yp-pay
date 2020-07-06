package com.yp.pay.wx.controller;

import com.yp.pay.base.controller.BaseController;
import com.yp.pay.wx.service.WxPayCallBackService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: liuX
 * @date: 20191219
 * @description: WXPayAndAliPayController
 */

@RequestMapping("v1/wxPayCallBack")
@RestController
@Validated
@Api(value = "微信支付异步通知处理接口", produces = "application/json;charset=UTF-8")
public class WxPayCallBackController extends BaseController {

    @Autowired
    private WxPayCallBackService wxPayCallBackService;

    @ApiOperation("微信扫码支付异步通知结果处理")
    @PostMapping("/scanPayNotify")
    public void scanPayNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {

        wxPayCallBackService.scanPayNotify(request, response);
    }

    @ApiOperation("微信退款异步通知结果处理")
    @PostMapping("/refundNotify")
    public void refundNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {

        wxPayCallBackService.refundNotify(request, response);
    }

}
