package com.yp.pay.common.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
	
	public static String getMD5(String md5Str) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			byte[] bytes = md5.digest(md5Str.getBytes());
			StringBuffer sb = new StringBuffer();
			String temp = "";
			for (byte b : bytes) {
				temp = Integer.toHexString(b & 0XFF);
				sb.append(temp.length() == 1 ? "0" + temp : temp);
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}
	
	public static String getMD5(String md5Str,String charset) throws UnsupportedEncodingException {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			byte[] bytes = md5.digest(md5Str.getBytes(charset));
			StringBuffer sb = new StringBuffer();
			String temp = "";
			for (byte b : bytes) {
				temp = Integer.toHexString(b & 0XFF);
				sb.append(temp.length() == 1 ? "0" + temp : temp);
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}

}
