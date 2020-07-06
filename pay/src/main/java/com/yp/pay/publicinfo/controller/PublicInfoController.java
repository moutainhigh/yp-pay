package com.yp.pay.publicinfo.controller;

import com.yp.pay.base.controller.BaseController;
import com.yp.pay.publicinfo.service.PublicInfoService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: 公共服务接口
 * @author: liuX
 * @time: 2020/6/27 17:00
 */

@RestController
@RequestMapping("v1/publicInfo")
@Api(value = "公共服务接口", produces = "application/json;charset=UTF-8")
public class PublicInfoController extends BaseController {

    @Autowired
    private PublicInfoService publicInfoService;


}
