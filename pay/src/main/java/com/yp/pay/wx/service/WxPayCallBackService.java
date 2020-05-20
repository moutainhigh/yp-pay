package com.yp.pay.wx.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface WxPayCallBackService {

    void scanPayNotify(HttpServletRequest request, HttpServletResponse response) throws Exception;

    void refundNotify(HttpServletRequest request, HttpServletResponse response) throws Exception;
}
