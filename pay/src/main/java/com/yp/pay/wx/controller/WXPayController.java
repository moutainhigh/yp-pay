package com.yp.pay.wx.controller;

import com.yp.pay.base.controller.BaseController;
import com.yp.pay.base.entity.StandResponse;
import com.yp.pay.base.exception.BusinessException;
import com.yp.pay.entity.aliandwx.dto.*;
import com.yp.pay.entity.aliandwx.req.*;
import com.yp.pay.wx.service.WXPayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("v1/wxPay")
@Api(value = "微信支付相关接口", produces = "application/json;charset=UTF-8")
public class WXPayController extends BaseController {

    @Autowired
    WXPayService wxPayService;

    @ApiOperation(value = "下单支付（被扫，商户扫码枪扫描用户二维码）")
    @RequestMapping(value = "/microPay", method = RequestMethod.POST)
    public StandResponse<String> microPay(@RequestBody @Valid WXMicroPayReq microPayReq) throws BusinessException {
        return success(wxPayService.microPay(microPayReq));
    }

    /**
     * 详细说明 该接口支持微信的三种支付方式
     *
     * JSAPI支付（公众号）————目前使用该方式和支付宝的手机网站支付做成了一个动态二维码的聚合支付。
     *      提供一个动态二维码，二维码（实际就是一个连接地址）含有商品订单信息，扫码后（支付宝或者微信扫码）跳转商户的展示
     *      订单详情界面，如果支付宝扫码进来的，那么点击支付就是调用支付宝的手机网站支付的产品，拉起输入密码界面完成支付。
     *      如果微信扫码支付跳转的商户页面，点击支付，那么就直接拉起微信支付收入密码界面。
     *
     * APP支付————用户在商户的APP上，选择完商品后，点击支付完成对微信接口的调用，然后拉起微信的密码输入界面，输入密码完成支
     *      付，然后返回商户APP界面展示支付结果
     *
     * 注：上面两种方式，支付过程如下：用户选择完成商品（或者用户扫描二维码）后，先调用下面【统一下单接口】，该接口是预支付
     * 接口，通过接口会获取到预支付ID和签名信息，然后前台页面直接通过预支付ID等信息，调用微信的JS拉起支付密码输入界面，完成
     * 支付。
     * @param wxUnifiedPayReq
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "统一下单（JSAPI-JSAPI支付（公众号） NATIVE-Native支付（用户扫码） APP-APP支付（APP拉起微信支付））")
    @RequestMapping(value = "/unifiedPay", method = RequestMethod.POST)
    public StandResponse<ScanCodeDTO> unifiedPay(@RequestBody @Valid WXUnifiedPayReq wxUnifiedPayReq) throws Exception {
        return success(wxPayService.unifiedPay(wxUnifiedPayReq));
    }

    @ApiOperation(value = "订单查询")
    @RequestMapping(value = "/orderQuery", method = RequestMethod.POST)
    public StandResponse<TradePaymentRecordDTO> orderQuery(@RequestBody @Valid WXOrderQueryOrReverseReq orderQueryOrReverseReq) throws Exception {
        return success(wxPayService.orderQuery(orderQueryOrReverseReq));
    }

    @ApiOperation(value = "关闭订单", notes = "1、商户订单支付失败需要生成新单号重新发起支付，要对原订单号调用关单，避免重复支付；" +
            "2、系统下单后，用户支付超时，系统退出不再受理，避免用户继续，请调用关单接口。" +
            "注意：订单生成后不能马上调用关单接口，最短调用时间间隔为5分钟。")
    @RequestMapping(value = "/closeOrder", method = RequestMethod.POST)
    public StandResponse<CloseOrderDTO> closeOrder(@RequestBody @Valid WXCloseOrderReq wxCloseOrderReq) throws Exception {
        return success(wxPayService.closeOrder(wxCloseOrderReq));
    }

    @ApiOperation(value = "撤销订单")
    @RequestMapping(value = "/reverse", method = RequestMethod.POST)
    public Map<String, String> reverse(@RequestBody @Valid WXOrderQueryOrReverseReq orderQueryOrReverseReq) throws Exception {
        return wxPayService.reverse(orderQueryOrReverseReq);
    }

    @ApiOperation(value = "申请退款", notes = "1、交易时间超过一年的订单无法提交退款")
    @RequestMapping(value = "/refund", method = RequestMethod.POST)
    public StandResponse<ApplyRefundDTO> refund(@RequestBody @Valid WXRefundReq refundReq) throws Exception {
        return success(wxPayService.refund(refundReq));
    }

    @ApiOperation(value = "退款查询", notes = "注：提交退款申请后，通过调用该接口查询退款状态。退款有一定延时，用零钱支付的退款20分钟内到账，银行卡支付的退款3个工作日后重新查询退款状态。")
    @RequestMapping(value = "/refundQuery", method = RequestMethod.POST)

    // TODO 未测试 测试测试 123
    public StandResponse<RefundQueryDTO> refundQuery(@RequestBody @Valid WXRefundQueryReq refundQueryReq) throws Exception {
        return success(wxPayService.refundQuery(refundQueryReq));
    }

    @ApiOperation(value = "对账单下载")
    @RequestMapping(value = "/downloadBill", method = RequestMethod.POST)
    public StandResponse<BillDownloadDTO> downloadBill(@RequestBody @Valid WXDownloadBillReq wxDownloadBillReq) throws Exception {
        return success(wxPayService.downloadBill(wxDownloadBillReq));
    }

    @ApiOperation(value = "授权码OPENID查询接口")
    @RequestMapping(value = "/authCodeToOpenid/{merchantNo}", method = RequestMethod.GET)
    public Map<String, String> authCodeToOpenid(@PathVariable("merchantNo") @Valid String merchantNo) throws Exception {
        return wxPayService.authCodeToOpenid(merchantNo);
    }

    @ApiOperation(value = "微信获取openId接口")
    @RequestMapping(value = "/getOpenId", method = RequestMethod.GET)
    public StandResponse<String> getOpenId(@RequestParam("code") String code,
                                           @RequestParam("merchantNo") String merchantNo) throws BusinessException {
        if (StringUtils.isBlank(merchantNo)) {
            throw new BusinessException("请求平台的商户编号不能为空。");
        }
        if (StringUtils.isBlank(code)) {
            throw new BusinessException("code不能为空，请核实请求数据");
        }
        return success(wxPayService.getOpenId(code, merchantNo));
    }

    @ApiOperation(value = "获取指定商户编号的商户信息")
    @RequestMapping(value = "/merchantQuery", method = RequestMethod.GET)
    public StandResponse<MerchantInfoDTO> merchantQuery(@RequestParam("merchantNo") String merchantNo,
                                                        @RequestParam("payWayCode") String payWayCode) throws BusinessException {
        if (StringUtils.isBlank(merchantNo)) {
            throw new BusinessException("请求平台的商户编号不能为空。");
        }
        if (StringUtils.isBlank(payWayCode)) {
            throw new BusinessException("请求交易渠道代码不能为空。");
        }
        return success(wxPayService.merchantQuery(merchantNo,payWayCode));
    }

    /**
     * 统一下单获取二维码支持聚合支付
     *
     * 微信支付流程：
     * 下单后，会返回二维码信息，该二维码可以被支付宝扫码，也可以被微信扫码。
     * 该二维码中含有订单相关信息（金额，商品描述，商品详情等），是一个动态二维码信息。
     * 1、用户使用微信扫描该二维码，前端获取用户的code,然后通过code调用中台的getOpenId接口获取openid
     * 2、获取openID后，前端调用中台统一下单接口（JSAPI-JSAPI支付），获取微信的预下单ID
     * 3、前端页面拿到预下单ID，直接通过js调用微信支付接口完成支付
     *
     * @param qrCodeInfoReq
     * @return
     * @throws BusinessException
     */
    @ApiOperation(value = "获取统一下单二维码信息")
    @RequestMapping(value = "/getQrCodeInfo", method = RequestMethod.POST)
    public StandResponse<ScanCodeDTO> getQrCodeInfo(@RequestBody @Valid QrCodeInfoReq qrCodeInfoReq) throws BusinessException {
        return success(wxPayService.getQrCodeInfo(qrCodeInfoReq));
    }

    @ApiOperation(value = "通过订单号和商户编号获取订单信息")
    @RequestMapping(value = "/getTradeOrderInfo", method = RequestMethod.GET)
    public StandResponse<TradePaymentRecordDTO> getTradeOrderInfo(@RequestParam("merchantNo") String merchantNo,
                                                                  @RequestParam("orderNo") String orderNo) throws BusinessException {
        if (StringUtils.isBlank(merchantNo)) {
            throw new BusinessException("请求平台的商户编号不能为空。");
        }
        if (StringUtils.isBlank(orderNo)) {
            throw new BusinessException("订单号不能为空，请核实请求数据");
        }
        return success(wxPayService.getTradeOrderInfo(merchantNo, orderNo));
    }

}
