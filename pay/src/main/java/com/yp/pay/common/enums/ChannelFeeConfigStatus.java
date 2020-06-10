package com.yp.pay.common.enums;

/**
 * 商户渠道支付方式费率配置表[merchant_channel_fee]中费率启用状态
 *
 * @author: liuX
 * @date: 2020/5/13 10:16
 * @description: ChannelFeeConfigStatus
 */
public enum ChannelFeeConfigStatus implements  BaseEnum{

    /**
     * 未启用
     */
    UN_USE(0, "未启用"),

    /**
     * 启用
     */
    USE(1, "启用");

    /**
     * 枚举代码
     */
    private Integer code;

    /**
     * 枚举信息
     */
    private String message;

    ChannelFeeConfigStatus(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ChannelFeeConfigStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }

        for (ChannelFeeConfigStatus status : values()) {
            if (code.equals(status.getCode())) {
                return status;
            }
        }
        return null;
    }
}
