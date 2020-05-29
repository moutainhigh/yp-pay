package com.yp.pay.wx.controller;

import com.yp.pay.base.controller.BaseController;
import com.yp.pay.base.entity.StandResponse;
import com.yp.pay.base.exception.BusinessException;
import com.yp.pay.common.util.EntityConverter;
import com.yp.pay.entity.dto.*;
import com.yp.pay.entity.req.*;
import com.yp.pay.wx.service.WxPayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @description: 微信分账支付相关接口
 *
 * @author: liuX
 * @time: 2020/5/26 22:50
 */
@RestController
@RequestMapping("v1/wxProfitPay")
@Api(value = "微信分账支付相关接口", produces = "application/json;charset=UTF-8")
public class WxProfitPayController extends BaseController {

    private static final String PROFIT_SHARE = "Y";

    @Autowired
    WxPayService wxPayService;

    @ApiOperation(value = "带分账功能的付款码支付（被扫，商户扫码枪扫描用户二维码）")
    @RequestMapping(value = "/microPayWithProfitShare", method = RequestMethod.POST)
    public StandResponse<String> microPayWithProfitShare(@RequestBody @Valid WxMicroPayReq microPayReq) throws BusinessException {

        microPayReq.setProfitShare(PROFIT_SHARE);
        return success(wxPayService.microPay(microPayReq));
    }

    /**
     * @Description 用户扫描商户二维码完成支付（该接口获取二维码）。
     * 使用场景：可用于商户网站，用户选完商品到结账页面，如果用户选择微信二维码支付，直接展示支付二维码，用户直接扫码即可完成支付。
     *
     * 说明：该接口直接获取待支付的二维码信息，该二维码有效期为2小时。
     * 获取到二维码后，如果2小时未支付，则会自动失效，无法完成支付。
     *
     * @Author liuX
     * @Date 2020/5/16/ 6:46
     * @Param [wxUnifiedPayReq]
     */
    @ApiOperation(value = "带分账功能的微信扫码支付，用户打开微信扫一扫，扫描商家二维码完成支付（该接口获取二维码）。" )
    @RequestMapping(value = "/userScanPayWithProfitShare", method = RequestMethod.POST)
    public StandResponse<ScanCodeDTO> userScanPayWithProfitShare(@RequestBody @Valid WxUserScanPayReq wxUserScanPayReq) throws Exception {

        wxUserScanPayReq.setProfitShare(PROFIT_SHARE);

        wxUserScanPayReq.setTradeType("NATIVE");
        WxUnifiedPayReq wxUnifiedPayReq = EntityConverter.copyAndGetSingle(wxUserScanPayReq, WxUnifiedPayReq.class);

        return success(wxPayService.unifiedPay(wxUnifiedPayReq));
    }

    /**
     *  @Description 聚合支付:调用微信JSPAI
     *
     * 目前使用该方式和支付宝的手机网站支付做成了一个动态二维码的聚合支付。
     * 提供一个动态二维码(该接口上一个接口)，用户使用支付宝或者微信扫码后，
     * 跳转商户的展示订单详情界面
     * 1、支付宝扫码，跳转的商户页面，点击支付调用支付宝的手机网站支付的产品，拉起输入密码界面完成支付。
     * 2、微信扫码，跳转的商户页面，点击支付调用微信JSAPI支付，获取预支付ID，然后本地通过微信JS，拉起微信支付收入密码界面。
     *
     * @Author liuX
     * @Date 2020/5/16/ 7:07
     * @Param [wxjsPayReq]
     */
    @ApiOperation(value = "带分账功能的微信公众号支付(JSAPI支付) 用户通过微信扫码，关注工作号等方式进入商家H5页面，并在微信内调用JSSDK完成支付（该接口获取预支付ID等信息）。" )
    @RequestMapping(value = "/qrCodeAggregationPayWithProfitShare", method = RequestMethod.POST)
    public StandResponse<ScanCodeDTO> qrCodeAggregationPayWithProfitShare(@RequestBody @Valid WxJsPayReq wxjsPayReq) throws Exception {

        wxjsPayReq.setProfitShare(PROFIT_SHARE);

        wxjsPayReq.setTradeType("JSAPI");
        WxUnifiedPayReq wxUnifiedPayReq = EntityConverter.copyAndGetSingle(wxjsPayReq, WxUnifiedPayReq.class);
        wxUnifiedPayReq.setAggregationPay(true);

        return success(wxPayService.unifiedPay(wxUnifiedPayReq));
    }

    /**
     * @Description 公众号支付(JSAPI支付)
     *
     * 使用在微信公众号或者在微信内部打开的网页，调用JSAPI接口获取预支付ID，然后通过微信浏览器直接调用微信js，拉起微信的支付窗口完成支付
     *
     * @Author liuX
     * @Date 2020/5/16/ 7:07
     * @Param [wxjsPayReq]
     */
    @ApiOperation(value = "带分账功能的微信公众号支付(JSAPI支付) 用户通过微信扫码，关注工作号等方式进入商家H5页面，并在微信内调用JSSDK完成支付（该接口获取预支付ID等信息）。" )
    @RequestMapping(value = "/jsApiPayWithProfitShare", method = RequestMethod.POST)
    public StandResponse<ScanCodeDTO> jsApiPayWithProfitShare(@RequestBody @Valid WxJsPayReq wxjsPayReq) throws Exception {

        wxjsPayReq.setProfitShare(PROFIT_SHARE);

        wxjsPayReq.setTradeType("JSAPI");
        WxUnifiedPayReq wxUnifiedPayReq = EntityConverter.copyAndGetSingle(wxjsPayReq, WxUnifiedPayReq.class);
        wxUnifiedPayReq.setAggregationPay(false);

        return success(wxPayService.unifiedPay(wxUnifiedPayReq));
    }

    /**
     * @Description APP支付
     *
     * 用户在商户的APP上，选择完商品后，点击支付完成对微信接口的调用，然后拉起微信的密码输入界面，输入密码完成支
     * 付，然后返回商户APP界面展示支付结果
     *
     * APP支付与JSAPI支付的区别：
     *  APP支付是在微信浏览器外部唤起微信支付界面
     *  JSAPI支付是在微信浏览器内部唤起微信支付界面
     *
     * @Author liuX
     * @Date 2020/5/16/ 7:30
     * @Param [wxAppPayReq]
     */
    @ApiOperation(value = "带分账功能的APP支付（APP拉起微信支付，该接口获取预支付ID等信息）")
    @RequestMapping(value = "/appPayWithProfitShare", method = RequestMethod.POST)
    public StandResponse<WxAppPayDTO> appPayWithProfitShare(@RequestBody @Valid WxAppPayReq wxAppPayReq) throws Exception {

        wxAppPayReq.setProfitShare(PROFIT_SHARE);

        wxAppPayReq.setTradeType("APP");
        WxUnifiedPayReq wxUnifiedPayReq = EntityConverter.copyAndGetSingle(wxAppPayReq, WxUnifiedPayReq.class);

        return success(wxPayService.appPay(wxUnifiedPayReq));
    }

}
