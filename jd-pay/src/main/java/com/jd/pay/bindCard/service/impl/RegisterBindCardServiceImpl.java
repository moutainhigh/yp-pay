package com.jd.pay.bindCard.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.jd.pay.base.exception.BusinessException;
import com.jd.pay.bindCard.entity.req.BindCardInfoReq;
import com.jd.pay.bindCard.entity.req.RegisterReq;
import com.jd.pay.bindCard.service.RegisterBindCardService;
import com.jd.pay.handler.JDPayHandlerService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class RegisterBindCardServiceImpl implements RegisterBindCardService {

    private static final String SUCCESS = "OOOO";

    @Autowired
    private JDPayHandlerService jdPayHandlerService;

    @Override
    public String memberRegister(RegisterReq registerReq) throws BusinessException{

        Map<String,String> map = new HashMap<>();
        map.put("userCode",registerReq.getUserCode());
        map.put("cellPhone",registerReq.getCellPhone());

        JSONObject jsonObject = jdPayHandlerService.memberRegister(map);

        String responseCode = jsonObject.getString("jsonObject");
        if(SUCCESS.equals(responseCode)){
            return jsonObject.getString("partnerMemberId");
            // TODO 存储数据库
        }else{
            throw new BusinessException(jsonObject.getString("responseDesc"));
        }
    }

    @Override
    public String bindCard(BindCardInfoReq bindCardInfoReq)  throws BusinessException{
        return null;
    }
}
