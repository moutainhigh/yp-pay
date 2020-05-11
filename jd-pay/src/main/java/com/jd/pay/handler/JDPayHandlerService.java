package com.jd.pay.handler;

import com.alibaba.fastjson.JSONObject;
import com.jd.pay.base.exception.BusinessException;

import java.util.Map;

public interface JDPayHandlerService {

    JSONObject memberRegister(Map<String, String> map) throws BusinessException;

    JSONObject bindBankCard(Map<String, String> map) throws BusinessException;
}
