package com.yp.pay.wx.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @description: 微信异步通知处理接口
 *
 * @author: liuX
 * @time: 2020/5/29 11:11
 */
public interface WxPayCallBackService {

    /**
     * 处理微信支付异步通知
     *
     * @author: liuX
     * @time: 2020/5/29 11:06
     * @params: request 请求request
     * @params: response 返回response
     * @exception Exception 异常
     */
    void scanPayNotify(HttpServletRequest request, HttpServletResponse response) throws Exception;

    /**
     * 处理微信退款异步通知
     *
     * @author: liuX
     * @time: 2020/5/29 11:07
     * @params: request
     * @params: response
     * @exception Exception
     */
    void refundNotify(HttpServletRequest request, HttpServletResponse response) throws Exception;
}
