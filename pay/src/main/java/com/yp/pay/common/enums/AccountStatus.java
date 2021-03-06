package com.yp.pay.common.enums;

/**
 * 商户渠道配置表[merchant_pay_info]中的商户状态
 *
 * @author: liuX
 * @date: 2020/5/13 10:16
 * @description: 账户状态
 */
public enum AccountStatus implements  BaseEnum{
    /**
     * 已开户
     */
    ACTIVE(1, "正常"),
    /**
     * 冻结
     */
    FREEZE(0, "冻结");

    /**
     * 枚举代码
     */
    private Integer code;

    /**
     * 枚举信息
     */
    private String message;

    AccountStatus(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static AccountStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }

        for (AccountStatus status : values()) {
            if (code.equals(status.getCode())) {
                return status;
            }
        }
        return null;
    }
}
