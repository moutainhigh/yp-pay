package com.yp.pay.wx.controller;


import com.yp.pay.base.controller.BaseController;
import com.yp.pay.base.entity.StandResponse;
import com.yp.pay.base.exception.BusinessException;
import com.yp.pay.entity.aliandwx.dto.WXProfitShareQueryDTO;
import com.yp.pay.entity.aliandwx.dto.WXProfitShareReceiverAddDTO;
import com.yp.pay.entity.aliandwx.dto.WXProfitShareReturnDTO;
import com.yp.pay.entity.aliandwx.dto.WXSingleProfitShareDTO;
import com.yp.pay.entity.aliandwx.req.*;
import com.yp.pay.wx.service.WXProfitShareService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 微信分账功能相关接口
 * <p>
 * 说明：当用户调用了带有分账标示的支付接口，该笔支付结果的资金将会被冻结，直到超过冻结期限 或者 主动调用分账并完结分账后，该笔资金才
 * 以解冻的状态在商户平台上自由处理。
 *
 * @author liuX
 * @date 20200519
 */
@RestController
@RequestMapping("v1/wxProfitShare")
@Api(value = "微信分账相关接口", produces = "application/json;charset=UTF-8")
public class WXProfitShareController extends BaseController {

    @Autowired
    WXProfitShareService wxProfitShareService;


    /**
     * 单次分账（分账后剩余资金会自动解冻到商户账户，不需要调用完结分账接口）
     *
     * @return WXSingleProfitShareDTO
     * @throws BusinessException
     */
    @ApiOperation(value = "单次分账（分账后剩余资金会自动解冻到商户账户，不需要调用完结分账接口）")
    @RequestMapping(value = "/singleProfitShare", method = RequestMethod.POST)
    public StandResponse<WXSingleProfitShareDTO> singleProfitShare(@RequestBody @Valid WXProfitShareSingleReq
                                                                           wxProfitShareSingleReq) throws BusinessException {

        return success(wxProfitShareService.singleProfitShare(wxProfitShareSingleReq));
    }


    /**
     * 多次分账（因为是多次分账，所以不知道什么时候完成分账，所以业务认为分账完成后需要调用完结分账接口）
     *
     * @return
     * @throws BusinessException
     */
    @ApiOperation(value = "多次分账")
    @RequestMapping(value = "/multiProfitShare", method = RequestMethod.POST)
    public StandResponse<WXSingleProfitShareDTO> multiProfitShare(@RequestBody @Valid WXProfitShareSingleReq
                                                                          wxProfitShareSingleReq) throws BusinessException {

        return success(wxProfitShareService.multiProfitShare(wxProfitShareSingleReq));
    }

    /**
     * 查询分账结果
     *
     * @return
     * @throws BusinessException
     */
    @ApiOperation(value = "查询分账结果")
    @RequestMapping(value = "/profitShareQuery", method = RequestMethod.POST)
    public StandResponse<WXProfitShareQueryDTO> profitShareQuery(@RequestBody @Valid WXProfitShareQueryReq
                                                                         wxProfitShareQueryReq) throws BusinessException {

        return success(wxProfitShareService.profitShareQuery(wxProfitShareQueryReq));
    }

    /**
     * 添加分账接收方
     * 说明：目前微信只支持一个一个的添加，不能同时上传多个分账接收方
     *
     * @return
     * @throws BusinessException
     */
    @ApiOperation(value = "添加分账接收方")
    @RequestMapping(value = "/profitShareAddReceiver", method = RequestMethod.POST)
    public StandResponse<WXProfitShareReceiverAddDTO> profitShareAddReceiver(@RequestBody @Valid WXProfitShareReceiverAddReq
                                                                                     wxProfitShareReceiverAddReq) throws BusinessException {

        return success(wxProfitShareService.profitShareAddReceiver(wxProfitShareReceiverAddReq));
    }

    /**
     * 删除分账接收方
     *
     * @return
     * @throws BusinessException
     */
    @ApiOperation(value = "删除分账接收方")
    @RequestMapping(value = "/profitShareRemoveReceiver", method = RequestMethod.POST)
    public StandResponse<WXProfitShareReceiverAddDTO> profitShareRemoveReceiver(@RequestBody @Valid WXProfitShareReceiverSimpleReq
                                                                                        wxProfitShareReceiverSimpleReq) throws BusinessException {

        return success(wxProfitShareService.profitShareRemoveReceiver(wxProfitShareReceiverSimpleReq));
    }

    /**
     * 完結分账
     *
     * @return
     * @throws BusinessException
     */
    @ApiOperation(value = "完結分账")
    @RequestMapping(value = "/profitShareFinish", method = RequestMethod.POST)
    public StandResponse<WXSingleProfitShareDTO> profitShareFinish(@RequestBody @Valid WXProfitShareFinishReq
                                                                           wxProfitShareFinishReq) throws BusinessException {

        return success(wxProfitShareService.profitShareFinish(wxProfitShareFinishReq));
    }

    /**
     * 分账回退
     * <p>
     * 在分账完成后，但是订单需要退款，这个时候就需要分账回退，然后在将商户的钱退款到用户账户
     *
     * @return
     * @throws BusinessException
     */
    @ApiOperation(value = "分账回退")
    @RequestMapping(value = "/profitShareReturn", method = RequestMethod.POST)
    public StandResponse<WXProfitShareReturnDTO> profitShareReturn(@RequestBody @Valid WXProfitShareReturnReq
                                                                           wxProfitShareReturnReq) throws BusinessException {

        return success(wxProfitShareService.profitShareReturn(wxProfitShareReturnReq));
    }

    /**
     * 分账回退结果查询
     *
     * @return
     * @throws BusinessException
     */
    @ApiOperation(value = "分账回退结果查询")
    @RequestMapping(value = "/profitShareReturnQuery", method = RequestMethod.POST)
    public StandResponse<WXProfitShareReturnDTO> profitShareReturnQuery(@RequestBody @Valid WXProfitShareReturnQueryReq
                                                                                wxProfitShareReturnQueryReq) throws BusinessException {

        return success(wxProfitShareService.profitShareReturnQuery(wxProfitShareReturnQueryReq));
    }
}
