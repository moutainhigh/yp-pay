package com.jd.pay.handler.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jd.jr.jropen.unifySdk.newApi.RealNameRegistrationService;
import com.jd.jr.jropen.unifySdk.reqModel.RegisterRequest;
import com.jd.jr.jropen.unifySdk.respModel.RegisterResponse;
import com.jd.pay.base.exception.BusinessException;
import com.jd.pay.handler.JDPayHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Service
public class JDPayHandlerServiceImpl implements JDPayHandlerService {

    Logger logger = LoggerFactory.getLogger(getClass());

    private static RealNameRegistrationService realNameRegistrationService = new RealNameRegistrationService();

    private JSONObject commonSendAndReceive(String type, RegisterRequest registerRequestBody) throws BusinessException{

        RegisterResponse registerRespModel = null;

        switch (type) {
            case "register":
                registerRespModel = realNameRegistrationService.register(registerRequestBody);
                break;
            case "1":
                break;
            case "2":
                break;
            case "3":
                break;
            case "4":
                break;
            case "5":
                break;
            case "6":
                break;
            case "7":
                break;
            case "8":
                break;
            case "9":
                break;
            default:
                registerRespModel = null;
                break;
        }

        logger.info("请求接口：" + type + "，请求参数：" + JSON.toJSONString(registerRequestBody));
        String response;
        if (registerRespModel != null) {
            response = JSON.toJSONString(registerRespModel);
        }else{
            throw new BusinessException("请求接口不存在，和联系开发人员核对接口");
        }
        logger.info("接口名称：" + type + "，返回数据：" + response);
        return JSON.parseObject(response);
    }

    @Override
    public JSONObject memberRegister(Map<String, String> map)  throws BusinessException{

        RegisterRequest registerRequestBody = new RegisterRequest();
        //业务请求参数
        // 授权码 1：同意
        registerRequestBody.setAuthorizeFlag("1");
        // 手机号
        registerRequestBody.setPartnerMemberMobile(map.get("cellPhone"));
        // 用户编码
        registerRequestBody.setPartnerMemberId(map.get("userCode"));
        //原业务header参数
        // 京东提供的合作伙伴编号 TODO
        registerRequestBody.setPartnerId("");
        registerRequestBody.setRequestTime(new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()));
        registerRequestBody.setRequestId(String.valueOf(System.currentTimeMillis()));

        String type = "register";

        return commonSendAndReceive(type, registerRequestBody);
    }

    @Override
    public JSONObject bindBankCard(Map<String, String> map) throws BusinessException {
        return null;
    }
}
