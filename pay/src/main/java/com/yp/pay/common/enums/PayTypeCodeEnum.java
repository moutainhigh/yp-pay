package com.yp.pay.common.enums;

/**
 * @author: lijiang
 * @date: 2020/02/25 10:16
 * @description: PayTypeCodeEnum
 */
public enum PayTypeCodeEnum {
    /**
     * 支付宝被扫
     */
    ALI_F2F_PAY("ALI_F2F_PAY", "支付宝被扫"),
    /**
     * 支付宝网站支付
     */
    ALI_WEB_PAY("ALI_WEB_PAY", "支付宝网站支付");

    /**
     * 枚举代码
     */
    private String code;

    /**
     * 枚举信息
     */
    private String message;

    PayTypeCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static PayTypeCodeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }

        for (PayTypeCodeEnum payWay : values()) {
            if (code.equals(payWay.getCode())) {
                return payWay;
            }
        }
        return null;
    }

    /**
     * 取值
     *
     * @return String
     */
    public String getCode() {
        return this.code;
    }

    /**
     * 取描述信息
     *
     * @return
     */
    public String getMessage() {
        return this.message;
    }
}
