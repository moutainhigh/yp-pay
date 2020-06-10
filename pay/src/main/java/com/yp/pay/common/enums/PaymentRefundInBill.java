package com.yp.pay.common.enums;

/**
 * 渠道对账单下载数据表[bill_channel_info]中的退款状态
 *
 * @author: liuX
 * @date: 2020/5/13 10:16
 * @description: PaymentRefundInBill
 */
public enum PaymentRefundInBill implements  BaseEnum{

    /**
     * 退款完成
     */
    SUCCESS(0, "退款完成"),

    /**
     * 退款失败
     */
    FAIL(1, "退款失败");

    /**
     * 枚举代码
     */
    private Integer code;

    /**
     * 枚举信息
     */
    private String message;

    PaymentRefundInBill(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static PaymentRefundInBill getByCode(Integer code) {
        if (code == null) {
            return null;
        }

        for (PaymentRefundInBill status : values()) {
            if (code.equals(status.getCode())) {
                return status;
            }
        }
        return null;
    }
}
