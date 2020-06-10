package com.yp.pay.common.enums;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 终端枚举类
 *
 * @author: liuX
 * @date: 20200517 15:16
 * @description: TerminalType
 */
public enum TerminalType {

    COMPUTER(1, "电脑", new HashMap<String,Object>(){
        {
            put("CHZF", "01");
            put("PABC", 1);
        }
    }),
    ANDROID_PHONE(2, "安卓手机端", new HashMap<String,Object>(){
        {
            put("CHZF", "02");
            put("PABC", 2);
        }
    }),
    IOS_PHONE(3, "IOS手机端", new HashMap<String,Object>(){
        {
            put("CHZF", "03");
            put("PABC", 3);
        }
    }),
    WEARABLE_DEVICE(4, "可穿戴设备", new HashMap<String,Object>(){
        {
            put("CHZF", "04");
            put("PABC", 4);
        }
    }),
    DIGITAL_TV(5, "数字电视", new HashMap<String,Object>(){
        {
            put("CHZF", "05");
            put("PABC", 5);
        }
    }),
    QR_CODE_DEVICE(6, "条码支付受理终端", new HashMap<String,Object>(){
        {
            put("CHZF", "06");
            put("PABC", 6);
        }
    }),
    PAD_DEVICE(7, "平板设备", new HashMap<String,Object>(){
        {
            put("CHZF", "07");
            put("PABC", 7);
        }
    }),
    OTHER_DEVICE(99, "其他设备", new HashMap<String,Object>(){
        {
            put("CHZF", "99");
            put("PABC", 99);
        }
    });


    // 代号
    Integer code;
    // 描述
    String desc;

    Map<String, Object> channel;


    TerminalType(Integer code, String desc, Map<String, Object> channel) {
        this.code = code;
        this.desc = desc;
        this.channel = channel;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public Map<String, Object> getChannel() {
        return channel;
    }

    public static TerminalType getTerminalType(Integer code) {

        if (code == null) {
            return null;
        }

        for (TerminalType terminalType : values()) {
            if (terminalType.getCode().equals(code)) {
                return terminalType;
            }
        }

        return null;
    }

    public static Object getTerminalValue(Integer code,String channelCode){
        if(code == null){
            return null;
        }
        // 如果为空，默认传化支付
        if(StringUtils.isEmpty(channelCode)){
            channelCode = "CHZF";
        }
        for (TerminalType terminalType : values()) {
            if(Integer.compare(terminalType.getCode(),code)==0){
                return terminalType.getChannel().get(channelCode);
            }
        }
        return null;
    }

    public String toString() {

        StringBuffer stringBuffer = new StringBuffer();
        for (String key:channel.keySet()) {
            stringBuffer.append(key+":"+channel.get(key)+",");
        }

        String mapValue = stringBuffer.toString().substring(0,stringBuffer.length()-1);

        return "TerminalType{" +
                "code=" + code +
                ", desc='" + desc + '\'' +
                ", channel=[" + mapValue +"]}";
    }
}
