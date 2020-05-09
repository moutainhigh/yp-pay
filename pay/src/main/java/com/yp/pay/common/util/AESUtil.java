package com.yp.pay.common.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;
import java.util.Base64;

/**
 * 微信支付在报文传输从的加密解密工具代码
 * TODO
 */
public class AESUtil {

    /**
     * 加解密算法/工作模式/填充方式
     */
    private static final String ALGORITHM_MODE_PADDING = "AES/ECB/PKCS5Padding";

    /**
     * 密钥算法
     */
    private static final String ALGORITHM = "AES";

    /**
     * AES加密
     *
     * （1）使用秘钥字符串进行AES-256-ECB加密
     * （2）将加密后的字符串进行base64编码
     *
     * @param data 需要加密的信息
     * @param keyData 加密时使用的秘钥字符串
     * @return 返回加密后的密文字符串
     * @throws Exception
     */
    public static String encryptData(String data,String keyData) throws Exception {

        Security.addProvider(new BouncyCastleProvider());
        // 创建密码器
        Cipher cipher = Cipher.getInstance(ALGORITHM_MODE_PADDING, "BC");
        SecretKeySpec key = new SecretKeySpec(keyData.getBytes(), ALGORITHM);
        // 初始化
        cipher.init(Cipher.ENCRYPT_MODE, key);

        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(cipher.doFinal(data.getBytes()));
    }

    /**
     * AES解密
     *
     * （1）对加密串A做base64解码，得到加密串B
     * （2）用key*对加密串B做AES-256-ECB解密（PKCS7Padding）
     * @param base64Data 加密的密文数据
     * @param keyData 解密用的秘钥字符串
     * @return
     * @throws Exception
     */
    public static String decryptData(String base64Data,String keyData) throws Exception {
        // 对加密数据进行base64解码
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] decode = decoder.decode(base64Data);

        Security.addProvider(new BouncyCastleProvider());

        Cipher cipher = Cipher.getInstance(ALGORITHM_MODE_PADDING);
        SecretKeySpec key = new SecretKeySpec(keyData.getBytes(), ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);

        return new String(cipher.doFinal(decode));
    }
}
