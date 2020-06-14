package com.yp.pay.common.util;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符换处理
 *
 * @author liuX
 */
public class StringUtil {

    private final static Logger logger = LoggerFactory.getLogger(StringUtil.class);

    private static String[] parsePatterns = { "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM", "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss",
            "yyyy.MM.dd HH:mm", "yyyy.MM", "yyyyMMddHHmmss", "yyyy-MM-dd-HH:mm:ss" };

    /**
     * 判断传入参数是否非为空
     *
     * @param s 传入参数
     * @return 是否非空
     */
    public static boolean isNotNull(Object s) {
        return (null != s) && !("").equals(s);
    }

    /**
     * 获取字符串对象的值，并且去掉前后的空格。若字符串对象为空，则放回空串
     *
     * @param value
     * @return
     */
    public static String getValue(Object value) {
        return value == null || "null".equals(value) ? "" : (value.toString()).trim();
    }

    /**
     * 将JSONObject转换成Map对象
     *
     * @param jsonObject
     * @return
     */
    public static Map<String, Object> getMapFromJsonObject(JSONObject jsonObject) {
        return JSONObject.parseObject(jsonObject.toJSONString(), new TypeReference<Map<String, Object>>() {
        });
    }

    /**
     * 将Map存储的对象，转换为key=value&key=value的字符 注：所有数据都要参与签名 例
     * key1=value1&key2=&key3=value3&key4=&key5=value5
     *
     * @param reqMap  请求Map
     * @param charset 字符集
     * @return 字符串
     */
    public static String getStringFromMap(Map<String, Object> reqMap, String charset) {
        String reqstr = "";
        if (reqMap.isEmpty()) {
            return reqstr;
        }
        StringBuilder sb = new StringBuilder();
        int size = reqMap.size();
        int lastIndex = size - 1;
        Set<String> keys = reqMap.keySet();
        Object value = null;
        for (String key : keys) {
            if (key == null) {
                continue;
            }
            sb.append(key).append("=");
            System.out.println("key==>" + key);
            value = reqMap.get(key);
            if (value != null) {
                try {
                    String encode = URLEncoder.encode(value.toString(), charset);
                    System.out.println("value==>" + value);
                    sb.append(encode);
                } catch (UnsupportedEncodingException e) {
                    logger.error("", e);
                }
            }
            if (lastIndex > 0) {
                sb.append("&");
            }
            lastIndex--;
        }
        reqstr = sb.toString();
        logger.info("map参数转换为form数据格式:{}", reqstr);
        return reqstr;
    }

    /**
     * 将Map存储的对象，转换为key=value&key=value的字符 注：所有数据都要参与签名 例
     * key1=value1&key2=&key3=value3&key4=&key5=value5
     *
     * @param reqMap 请求Map
     * @return 字符串
     */
    public static String getStringFromMap(Map<String, String> reqMap) {

        String reqstr = "";
        if (!reqMap.isEmpty()) {
            StringBuffer sf = new StringBuffer();
            for (Entry<String, String> en : reqMap.entrySet()) {
                sf.append(en.getKey()).append("=").append(org.apache.commons.lang3.StringUtils.isBlank(en.getValue()) ? "" : en.getValue()).append("&");
            }

            reqstr = sf.substring(0, sf.length() - 1);
        }

        return reqstr;
    }

    /**
     * 带排序的组装字符串
     * <p>
     * 将Map存储的对象，转换为key=value&key=value的字符 注：所有数据都要参与签名 例
     * key1=value1&key2=&key3=value3&key4=&key5=value5
     *
     * @param reqMap 请求Map
     * @return 字符串
     */
    public static String getStringFromMapJustSort(Map<String, String> reqMap) {

        String result = "";

        if (!reqMap.isEmpty()) {

            StringBuffer contentSB = new StringBuffer();

            List<String> keys = new ArrayList<>(reqMap.keySet());
            Collections.sort(keys);

            for (String key : keys) {
                String value = getValue(reqMap.get(key));
                contentSB.append(key).append("=").append(value).append("&");
            }
            result = org.apache.commons.lang3.StringUtils.isBlank(contentSB.toString()) ? "" : contentSB.toString().substring(0, contentSB.toString().length() - 1);
        }
        return result;
    }

    /**
     * 将Map存储的对象，转换为key=value&key=value的字符 注: 1/在转换前先对MAP数据按照字段名的 ASCII码从小到大排序
     * 2/sign字段和value为空（或空串）的数据不参与签名
     *
     * @param reqMap 请求Map
     * @return 字符串
     */
    public static String getStringFromMapBySort(Map<String, String> reqMap) {

        String result = "";

        if (!reqMap.isEmpty()) {
            StringBuffer contentSB = new StringBuffer();
            List<String> keys = new ArrayList<>(reqMap.keySet());
            Collections.sort(keys);
            for (String key : keys) {
                String value = getValue(reqMap.get(key));
                if (!"sign".equals(key) && !"Sign".equals(key) && isNotNull(value)) {
                    contentSB.append(key).append("=").append(value).append("&");
                }
            }
            result = org.apache.commons.lang3.StringUtils.isBlank(contentSB.toString()) ? "" : contentSB.toString().substring(0, contentSB.toString().length() - 1);
        }
        return result;
    }

    /**
     * 将形如key=value&key=value的字符串转换为相应的Map对象
     *
     * @param result
     * @return
     */
    public static Map<String, String> convertStringToMap(String result) {
        Map<String, String> map = null;
        try {

            if (isNotNull(result)) {
                if (result.startsWith("{") && result.endsWith("}")) {
                    result = result.substring(1, result.length() - 1);
                }
                map = parseQString(result);
            }

        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
        return map;
    }

    /**
     * 解析应答字符串，生成应答要素
     *
     * @param str 需要解析的字符串
     * @return 解析的结果map
     * @throws UnsupportedEncodingException
     */
    public static Map<String, String> parseQString(String str) throws UnsupportedEncodingException {

        Map<String, String> map = new HashMap<>(8);
        int len = str.length();
        StringBuilder temp = new StringBuilder();
        char curChar;
        String key = null;
        boolean isKey = true;
        // 值里有嵌套
        boolean isOpen = false;
        char openName = 0;
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                // 遍历整个带解析的字符串
                // 取当前字符
                curChar = str.charAt(i);
                if (isKey) {
                    // 如果当前生成的是key
                    // 如果读取到=分隔符
                    if (curChar == '=') {
                        key = temp.toString();
                        temp.setLength(0);
                        isKey = false;
                    } else {
                        temp.append(curChar);
                    }
                } else {// 如果当前生成的是value
                    if (isOpen) {
                        if (curChar == openName) {
                            isOpen = false;
                        }

                    } else {// 如果没开启嵌套
                        // 如果碰到，就开启嵌套
                        if (curChar == '{') {
                            isOpen = true;
                            openName = '}';
                        }
                        if (curChar == '[') {
                            isOpen = true;
                            openName = ']';
                        }
                    }
                    // 如果读取到&分割符,同时这个分割符不是值域，这时将map里添加
                    if (curChar == '&' && !isOpen) {
                        putKeyValueToMap(temp, isKey, key, map);
                        temp.setLength(0);
                        isKey = true;
                    } else {
                        temp.append(curChar);
                    }
                }

            }
            putKeyValueToMap(temp, isKey, key, map);
        }
        return map;
    }

    private static void putKeyValueToMap(StringBuilder temp, boolean isKey, String key, Map<String, String> map) throws UnsupportedEncodingException {
        if (isKey) {
            key = temp.toString();
            if (key.length() == 0) {
                throw new RuntimeException("QString format illegal");
            }
            map.put(key, "");
        } else {
            if (key.length() == 0) {
                throw new RuntimeException("QString format illegal");
            }
            map.put(key, temp.toString());
        }
    }

    /**
     * 把金额为“元”的单位转化成金额为“分”的单位
     *
     * @param input
     * @return
     */
    public static String formatYuanToFen(String input) {
        String out = "";
        NumberFormat ft = NumberFormat.getInstance();
        Number nbInput;
        try {
            nbInput = ft.parse(input);
            double fInput = nbInput.doubleValue() * 100.0;
            ft.setGroupingUsed(false);
            ft.setMaximumFractionDigits(0);
            out = ft.format(fInput);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return out;
    }

    /**
     * 把金额为“分”的单位转化成金额为“元”的单位
     *
     * @param input
     * @return
     */
    public static String formatFenToYuan(String input) {

        BigDecimal orderAmount = new BigDecimal(input);
        BigDecimal decimal = new BigDecimal(100);

        input = String.valueOf(orderAmount.divide(decimal));

        return new DecimalFormat("###0.00").format(Double.parseDouble(input));
    }

    /**
     * 获取隐藏的银行卡号
     *
     * @param: [cardNo]
     * @return: java.lang.String
     * @date: 2019/5/16
     * @time: 19:02
     * @see [类、类#方法、类#成员]
     */
    public static String getHiddentCardNo(String cardNo) {
        int length = cardNo.length();
        String shortCardNo = cardNo;
        if (length > 4) {
            shortCardNo = org.apache.commons.lang3.StringUtils.substring(cardNo, length - 4);
        }
        return org.apache.commons.lang3.StringUtils.join("****", shortCardNo);
    }

    /**
     * 正则表达式验证
     *
     * @param regxStr 正则表达式字符串
     * @param str     需要匹配的字符串
     * @return 如果匹配返回 true 否则返回 false
     */
    public static boolean getValidator(String regxStr, String str) {
        Pattern pattern = Pattern.compile(regxStr);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    /**
     * 验证输入字符串日期格式是否合法
     * @param str 输入字符串日期 如20190620/2019-06-20/2019-06-20 20:12
     * @param formatter 格式化类型 yyyyMMdd/yyyy-MM-dd/yyyy-MM-dd hh:mm
     * @return BOOLEAN
     */
    public static boolean isValidDate(String str, String formatter) {
        // 传入日期和合适为空则直接返回false
        if(str==null||"".equals(str.trim())){
            return false;
        }
        if(formatter==null||"".equals(formatter.trim())){
            return false;
        }
        /*
         格式化之前先判断长度是否一致
         20180915和yyyyMMdd匹配 2018-09-18和yyyy-MM-dd匹配 20190915 10:20和yyyyMMdd hh:mm
         */
        if(str.length()!=formatter.length()){
            return false;
        }else{

            SimpleDateFormat format = new SimpleDateFormat(formatter);
            try {
                // 设置lenient为false. 否则SimpleDateFormat会比较宽松地验证日期，比如2007/02/29会被接受，并转换成2007/03/01
                format.setLenient(false);
                format.parse(str);
            } catch (ParseException e) {
                return false;
            }
        }

        return true;
    }

    /**
     * 将输入日期字符串按照格式转化成Date类型数据
     *
     * @param str 日期的字符串
     * @param formatter 需要转化的格式类型
     * @return DATE数据
     * @throws ParseException
     */
    public static Date getDateFromString(String str, String formatter){

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatter);

        Date date = null;
        try {
            date = simpleDateFormat.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }

    /**
     * 得到当前日期字符串 格式（yyyy-MM-dd） pattern可以为："yyyy-MM-dd" "HH:mm:ss" "E"
     */
    public static String getDate(String pattern) {
        return DateFormatUtils.format(new Date(), pattern);
    }

    public static Date addHours(Date date, int amount) {
        return add(date, 11, amount);
    }

    private static Date add(Date date, int calendarField, int amount) {
        validateDateNotNull(date);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(calendarField, amount);
        return c.getTime();
    }

    private static void validateDateNotNull(Date date) {
        Validate.isTrue(date != null, "The date must not be null", new Object[0]);
    }

    /**
     * 日期型字符串转化为日期 格式 { "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss",
     * "yyyy/MM/dd HH:mm", "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm" }
     */
    public static Date parseDate(Object str) {
        if (str == null) {
            return null;
        }
        try {
            return DateUtils.parseDate(str.toString(), parsePatterns);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 得到日期字符串 默认格式（yyyy-MM-dd） pattern可以为："yyyy-MM-dd" "HH:mm:ss" "E"
     */
    public static String formatDate(Date date, Object... pattern) {
        String formatDate;
        if (pattern != null && pattern.length > 0) {
            formatDate = DateFormatUtils.format(date, pattern[0].toString());
        } else {
            formatDate = DateFormatUtils.format(date, "yyyy-MM-dd");
        }
        return formatDate;
    }

    /**
     * @description: 将字符串日期按照指定格式转化成Date类型数据
     *
     * @author: liuX
     * @time: 2020/5/31 9:09
     * @params: date 字符串日期 pattern需要转化的格式
     * @return: Date类型的日期
     */
    public static Date formatDateValue(String dateValue, String pattern) {

        if(StringUtils.isBlank(dateValue)){
            logger.error("需要转化的请求日期不能为空");
        }
        if(StringUtils.isBlank(pattern)){
            logger.error("需要转化的请求日期格式设置不能为空");
        }

        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Date date = null;
        try {
            date = sdf.parse(dateValue);
        } catch (ParseException e) {
            logger.error("日期格式转化异常，请核对银行返回报文中的支付完成时间格式。");
        }
        return date;
    }

    /**
     * @description: 生成指定长度的随机字符串
     *
     * @author: liuX
     * @time: 2020/5/30 12:08
     * @params: length 指定长度
     * @return: String 返回字符换
     */
    public static String generateNonceStr(Integer length) {
        char[] nonceChars = new char[length];

        for(int index = 0; index < nonceChars.length; ++index) {
            nonceChars[index] = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(new SecureRandom().nextInt("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".length()));
        }

        return new String(nonceChars);
    }
}
