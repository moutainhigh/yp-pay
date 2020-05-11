package com.jd.pay.bindCard.controller;

import com.jd.pay.base.exception.BusinessException;
import com.jd.pay.bindCard.entity.req.BindCardInfoReq;
import com.jd.pay.bindCard.entity.req.RegisterReq;
import com.jd.pay.bindCard.service.RegisterBindCardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "v1/bindCard")
@Api(value = "京东支付绑卡相关接口", produces = "application/json;charset=UTF-8")
public class RegisterBindCardController {

    @Autowired
    private RegisterBindCardService registerBindCardService;

    @ApiOperation("会员注册")
    @RequestMapping(value = "/memberRegister",method = RequestMethod.POST)
    public String memberRegister(@RequestBody @Valid RegisterReq registerReq) throws BusinessException {
        return registerBindCardService.memberRegister(registerReq);
    }

    // TODO 需要将该接口分离成对公绑卡和对私绑卡
    @ApiOperation("绑定银行卡接口")
    @RequestMapping(value = "/bindCard",method = RequestMethod.POST)
    public void bindCard(@RequestBody @Valid BindCardInfoReq bindCardInfoReq) throws BusinessException {

    }

}
