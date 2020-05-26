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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * @description: 微信普通商户支付接口
 *
 * @author: liuX
 * @time: 2020/5/26 22:51
 */
@RestController
@RequestMapping("v1/wxPay")
@Api(value = "微信支付相关接口", produces = "application/json;charset=UTF-8")
public class WxPayController extends BaseController {

    @Autowired
    WxPayService wxPayService;

    @ApiOperation(value = "付款码支付（被扫，商户扫码枪扫描用户二维码）")
    @RequestMapping(value = "/microPay", method = RequestMethod.POST)
    public StandResponse<String> microPay(@RequestBody @Valid WxMicroPayReq microPayReq) throws BusinessException {
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
    @ApiOperation(value = "微信扫码支付，用户打开微信扫一扫，扫描商家二维码完成支付（该接口获取二维码）。" )
    @RequestMapping(value = "/userScanPay", method = RequestMethod.POST)
    public StandResponse<ScanCodeDTO> userScanPay(@RequestBody @Valid WxUserScanPayReq wxUserScanPayReq) throws Exception {

        wxUserScanPayReq.setTradeType("NATIVE");
        WxUnifiedPayReq wxUnifiedPayReq = EntityConverter.copyAndGetSingle(wxUserScanPayReq, WxUnifiedPayReq.class);

        return success(wxPayService.unifiedPay(wxUnifiedPayReq));
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
    public StandResponse<UnionPayCodeDTO> getQrCodeInfo(@RequestBody @Valid QrCodeInfoReq qrCodeInfoReq) throws BusinessException {
        return success(wxPayService.getQrCodeInfo(qrCodeInfoReq));
    }

    /**
     * @Description 公众号支付(JSAPI支付)
     *
     * 目前使用该方式和支付宝的手机网站支付做成了一个动态二维码的聚合支付。
     * 提供一个动态二维码(该接口上一个接口)，二维码（实际就是一个连接地址）含有商品订单信息
     * 扫码后（支付宝或者微信扫码）跳转商户的展示订单详情界面，如果支付宝扫码进来的，那么点
     * 击支付就是调用支付宝的手机网站支付的产品，拉起输入密码界面完成支付。
     *
     * 如：微信扫码支付跳转的商户页面，点击支付，那么就直接拉起微信支付收入密码界面。
     *
     * @Author liuX
     * @Date 2020/5/16/ 7:07
     * @Param [wxJsPayReq]
     */
    @ApiOperation(value = "微信公众号支付(JSAPI支付) 用户通过微信扫码，关注工作号等方式进入商家H5页面，并在微信内调用JSSDK完成支付（该接口获取预支付ID等信息）。" )
    @RequestMapping(value = "/jsApiPay", method = RequestMethod.POST)
    public StandResponse<ScanCodeDTO> jsApiPay(@RequestBody @Valid WxJsPayReq wxJsPayReq) throws Exception {

        wxJsPayReq.setTradeType("JSAPI");
        WxUnifiedPayReq wxUnifiedPayReq = EntityConverter.copyAndGetSingle(wxJsPayReq, WxUnifiedPayReq.class);

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
    @ApiOperation(value = "APP支付（APP拉起微信支付，该接口获取预支付ID等信息）")
    @RequestMapping(value = "/appPay", method = RequestMethod.POST)
    public StandResponse<WxAppPayDTO> appPay(@RequestBody @Valid WxAppPayReq wxAppPayReq) throws Exception {

        wxAppPayReq.setTradeType("APP");
        WxUnifiedPayReq wxUnifiedPayReq = EntityConverter.copyAndGetSingle(wxAppPayReq, WxUnifiedPayReq.class);

        return success(wxPayService.appPay(wxUnifiedPayReq));
    }

    @ApiOperation(value = "订单查询")
    @RequestMapping(value = "/orderQuery", method = RequestMethod.POST)
    public StandResponse<TradePaymentRecordDTO> orderQuery(@RequestBody @Valid WxOrderQueryOrReverseReq orderQueryOrReverseReq) throws Exception {

        String orderNo = orderQueryOrReverseReq.getOrderNo();
        String channelOrderNo = orderQueryOrReverseReq.getChannelOrderNo();
        if (StringUtils.isBlank(orderNo) && StringUtils.isBlank(channelOrderNo)) {
            throw new BusinessException("[商户订单号]和[微信订单号]不能同时为空,至少输入一个条件。");
        }

        return success(wxPayService.orderQuery(orderQueryOrReverseReq));
    }

    @ApiOperation(value = "关闭订单", notes = "1、商户订单支付失败需要生成新单号重新发起支付，要对原订单号调用关单，避免重复支付；" +
            "2、系统下单后，用户支付超时，系统退出不再受理，避免用户继续，请调用关单接口。" +
            "注意：订单生成后不能马上调用关单接口，最短调用时间间隔为5分钟。")
    @RequestMapping(value = "/closeOrder", method = RequestMethod.POST)
    public StandResponse<CloseOrderDTO> closeOrder(@RequestBody @Valid WxCloseOrderReq wxCloseOrderReq) throws Exception {
        return success(wxPayService.closeOrder(wxCloseOrderReq));
    }

    @ApiOperation(value = "撤销订单")
    @RequestMapping(value = "/reverse", method = RequestMethod.POST)
    public Map<String, String> reverse(@RequestBody @Valid WxOrderQueryOrReverseReq orderQueryOrReverseReq) throws Exception {
        return wxPayService.reverse(orderQueryOrReverseReq);
    }

    @ApiOperation(value = "申请退款", notes = "1、交易时间超过一年的订单无法提交退款")
    @RequestMapping(value = "/refund", method = RequestMethod.POST)
    public StandResponse<ApplyRefundDTO> refund(@RequestBody @Valid WxRefundReq refundReq) throws Exception {

        String originalChannelOrderNo = refundReq.getOriginalChannelOrderNo();
        String originalOrderNo = refundReq.getOriginalOrderNo();
        if(StringUtils.isBlank(originalChannelOrderNo) && StringUtils.isBlank(originalOrderNo)){
            throw new BusinessException("[微信订单号]和[商户订单号]不能同时为空，至少需要填写一项。");
        }
        return success(wxPayService.refund(refundReq));
    }

    // TODO 未测试 测试测试 123
    @ApiOperation(value = "退款查询", notes = "注：提交退款申请后，通过调用该接口查询退款状态。退款有一定延时，用零钱支付的退款20分钟内到账，银行卡支付的退款3个工作日后重新查询退款状态。")
    @RequestMapping(value = "/refundQuery", method = RequestMethod.POST)
    public StandResponse<RefundQueryDTO> refundQuery(@RequestBody @Valid WxRefundQueryReq refundQueryReq) throws Exception {
        String refundOrderNo = refundQueryReq.getRefundOrderNo();
        String channelRefundOrderNo = refundQueryReq.getChannelRefundOrderNo();
        if(StringUtils.isBlank(refundOrderNo) && StringUtils.isBlank(channelRefundOrderNo)){
            throw new BusinessException("[商户退款单号]和[微信退款单号]不能同时为空，请至少填一项。");
        }
        return success(wxPayService.refundQuery(refundQueryReq));
    }

    // 测试完成
    @ApiOperation(value = "对账单下载")
    @RequestMapping(value = "/downloadBill", method = RequestMethod.POST)
    public StandResponse<BillDownloadDTO> downloadBill(@RequestBody @Valid WxDownloadBillReq wxDownloadBillReq) throws Exception {
        return success(wxPayService.downloadBill(wxDownloadBillReq));
    }

    @ApiOperation(value = "付款码查询openId接口（该接口只适用于付款码支付，用户商户调用该接口成功，那么客户手机上展示的付款码就只能由该商户发起付款，除非用户手机付款码更新）")
    @RequestMapping(value = "/authCodeToOpenId", method = RequestMethod.GET)
    public Map<String, String> authCodeToOpenid(@RequestParam("authCode") String authCode,
                                                @RequestParam("merchantNo") String merchantNo) throws Exception {
        if (StringUtils.isBlank(authCode)) {
            throw new BusinessException("付款码信息不能为空，请核实请求数据。");
        }
        if (StringUtils.isBlank(merchantNo)) {
            throw new BusinessException("请求平台的商户编号不能为空，请核实请求数据。");
        }
        return wxPayService.authCodeToOpenid(authCode,merchantNo);
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
