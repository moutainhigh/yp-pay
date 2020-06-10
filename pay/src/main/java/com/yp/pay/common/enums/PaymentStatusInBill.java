package com.yp.pay.common.enums;

/**
 * 渠道对账单下载数据表[bill_channel_info]中的交易状态
 *
 * @author: liuX
 * @date: 2020/5/13 10:16
 * @description: PaymentStatusInBill
 */
public enum PaymentStatusInBill implements  BaseEnum{

    /**
     * 交易成功
     */
    SUCCESS(0, "交易成功"),

    /**
     * 已退款
     */
    REFUND(1, "已退款");

    /**
     * 枚举代码
     */
    private Integer code;

    /**
     * 枚举信息
     */
    private String message;

    PaymentStatusInBill(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static PaymentStatusInBill getByCode(Integer code) {
        if (code == null) {
            return null;
        }

        for (PaymentStatusInBill status : values()) {
            if (code.equals(status.getCode())) {
                return status;
            }
        }
        return null;
    }
}
