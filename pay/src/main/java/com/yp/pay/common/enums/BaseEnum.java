package com.yp.pay.common.enums;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;


/**
 * 抽象枚举接口
 *
 * @author qidongwei
 * @date 2018/12/6
 */
public interface BaseEnum {


    String DEFAULT_VALUE_NAME = "code";
    String DEFAULT_DESC_NAME = "message";

    /**
     * 取值
     *
     * @return Integer
     */
    default Integer getCode() {
        Field field = ReflectionUtils.findField(this.getClass(), DEFAULT_VALUE_NAME);
        if (field == null) {
            return null;
        }
        try {
            field.setAccessible(true);
            return Integer.parseInt(field.get(this).toString());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 取描述信息
     *
     * @return
     */
    default String getMessage() {
        Field field = ReflectionUtils.findField(this.getClass(), DEFAULT_DESC_NAME);
        if (field == null) {
            return null;
        }
        try {
            field.setAccessible(true);
            return field.get(this).toString();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
