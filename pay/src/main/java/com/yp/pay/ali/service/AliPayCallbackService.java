package com.yp.pay.ali.service;

import com.yp.pay.base.exception.BusinessException;
import com.yp.pay.entity.dto.AliCallBackInfoDTO;
import com.yp.pay.entity.dto.AliRefundCallBackInfoDTO;

import java.util.Map;

public interface AliPayCallbackService {

    AliCallBackInfoDTO dealAliPayCallBackData(Map<String, String> params) throws BusinessException;

    AliRefundCallBackInfoDTO dealAliRefundBackData(Map<String, String> params) throws BusinessException;

}
