package com.yp.pay.common.enums;

/**
 * 支付交易记录表[trade_payment_record]分账状态
 *
 * @author: liuX
 * @date: 2020/5/13 10:16
 * @description: ProfitShareStatus
 */
public enum ProfitShareStatus implements  BaseEnum{

    /**
     * 未分账
     */
    UN_SHARE(0, "未分账"),

    /**
     * 部分分账
     */
    SHARE_PART(1, "部分分账"),

    /**
     * 完结分账
     */
    SHARE_DONE(2, "完结分账"),

    /**
     * 分账回退
     */
    SHARE_REFUND(3, "分账回退");

    /**
     * 枚举代码
     */
    private Integer code;

    /**
     * 枚举信息
     */
    private String message;

    ProfitShareStatus(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ProfitShareStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }

        for (ProfitShareStatus status : values()) {
            if (code.equals(status.getCode())) {
                return status;
            }
        }
        return null;
    }
}
