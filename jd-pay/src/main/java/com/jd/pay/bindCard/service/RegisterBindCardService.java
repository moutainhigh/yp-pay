package com.jd.pay.bindCard.service;

import com.jd.pay.base.exception.BusinessException;
import com.jd.pay.bindCard.entity.req.BindCardInfoReq;
import com.jd.pay.bindCard.entity.req.RegisterReq;
import org.springframework.stereotype.Service;

@Service
public interface RegisterBindCardService {

    String memberRegister(RegisterReq registerReq) throws BusinessException;

    String bindCard(BindCardInfoReq bindCardInfoReq) throws BusinessException;
}
