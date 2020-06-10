package com.yp.pay.common.enums;

/**
 * 支付交易记录表[trade_payment_record]分账标志
 *
 * @author: liuX
 * @date: 2020/5/13 10:16
 * @description: ProfitShareSign
 */
public enum ProfitShareSign implements  BaseEnum{

    /**
     * 不分账
     */
    UN_SHARE(0, "不分账"),

    /**
     * 分账
     */
    SHARE(1, "分账");

    /**
     * 枚举代码
     */
    private Integer code;

    /**
     * 枚举信息
     */
    private String message;

    ProfitShareSign(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ProfitShareSign getByCode(Integer code) {
        if (code == null) {
            return null;
        }

        for (ProfitShareSign status : values()) {
            if (code.equals(status.getCode())) {
                return status;
            }
        }
        return null;
    }
}
