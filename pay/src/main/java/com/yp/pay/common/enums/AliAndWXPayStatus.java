package com.yp.pay.common.enums;

/**
 * @author: liuX
 * @date: 20200215
 * @description: 订单支付状态
 */
public enum AliAndWXPayStatus implements  BaseEnum{
    /**
     * 已提交
     */
    SUBMITTED(0, "已提交"),
    /**
     * 处理中
     */
    HANDING(1, "处理中"),
    /**
     * 交易成功
     */
    SUCCESS(2, "交易成功"),
    /**
     * 交易失败
     */
    FAIL(3, "交易失败"),
    /**
     * 已关闭
     */
    CLOSED(4, "已关闭"),
    /**
     * 退款中
     */
    REFUNDING(5, "退款中"),
    /**
     * 退款失败
     */
    REFUND_ERR(6, "退款失败"),
    /**
     * 已退款
     */
    REFUND(7, "已退款");

    /**
     * 枚举代码
     */
    private Integer code;

    /**
     * 枚举信息
     */
    private String message;

    AliAndWXPayStatus(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static AliAndWXPayStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }

        for (AliAndWXPayStatus status : values()) {
            if (code.equals(status.getCode())) {
                return status;
            }
        }
        return null;
    }
}
