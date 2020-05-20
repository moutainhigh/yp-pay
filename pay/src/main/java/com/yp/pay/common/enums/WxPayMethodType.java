package com.yp.pay.common.enums;

public enum WxPayMethodType {

    microPay(1,"付款码支付"),
    scanPay(11,"扫码支付"),
    orderQuery(2,"订单查询"),
    closeOrder(12,"关闭订单"),
    reverse(3,"撤销订单"),
    refund(4,"申请退款"),
    refundQuery(5,"退款查询"),
    downloadBill(6,"对账单下载"),
    authCodeToOpenid(7,"授权码OPENID查询接口");

    /**
     * 枚举代码
     */
    private Integer code;

    /**
     * 枚举信息
     */
    private String message;

    WxPayMethodType(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static WxPayMethodType getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (WxPayMethodType WXPayMethodType : values()) {
            if (code.equals(WXPayMethodType.getCode())) {
                return WXPayMethodType;
            }
        }
        return null;
    }
}