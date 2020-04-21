package com.yp.pay.common.util;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

public class HttpClient {

    private static final Logger log = LogManager.getLogger("HttpClient");
    private static MyX509TrustManager xtm = new MyX509TrustManager();
    private static MyHostnameVerifier hnv = new MyHostnameVerifier();
    private static final String REQ_CHARSET = "utf-8";
    private static final String RES_PCHARSET = "utf-8";
    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 30000;

    private String sessionID;

    public String getSessionID() {
        return sessionID;
    }

    public HttpClient() {
        this.sessionID = System.currentTimeMillis() + RandomStringUtils.randomNumeric(5);
    }

    /**
     * 向请求地址提交from表单数据
     *
     * @param serverUrl
     * @param body      表单参数
     * @return
     */
    public static String submitForm(String serverUrl, String body, String requestCharset, String responseCharset) {

        if (StringUtils.isBlank(serverUrl) || StringUtils.isBlank(body)) {
            log.warn("serverUrl/body is required");
            return "";
        }
        StringBuffer returncontent = new StringBuffer();
        try {
            URL url = new URL(serverUrl);
            HttpURLConnection urlconn = (HttpURLConnection) url.openConnection();
            urlconn.setRequestMethod("POST");
            urlconn.setDoOutput(true);
            urlconn.setConnectTimeout(10000);
            urlconn.setReadTimeout(20000);
            urlconn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
            urlconn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=" + requestCharset);
            urlconn.setRequestProperty("Connection", "close");
            OutputStream out = urlconn.getOutputStream();
            out.write(body.getBytes(requestCharset));
            out.flush();
            out.close();
            InputStream in = urlconn.getInputStream();
            // 读取返回数据
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, responseCharset));
            String line = null;
            while ((line = reader.readLine()) != null) {
                returncontent.append(line).append("\n");
            }

            reader.close();
            String returnValue = StringUtil.getValue(returncontent.toString());
            log.info("表单提交接收到返回数据:" + returnValue);
            return returnValue;
        } catch (MalformedURLException e) {
            log.error("MalformedURLException: ", e);
        } catch (IOException e) {
            if (e instanceof SocketTimeoutException) {
                log.warn(e.getMessage(), e);
            }
            log.error("check here: " + e.toString(), e);
        } catch (Exception e) {
            log.error("other exception");
        } finally {

        }

        return "";
    }

    public static String get(String serverUrl, String body) {

        if (StringUtils.isBlank(serverUrl)) {
            log.warn("serverUrl/body is required");
            return null;
        }
        try {
            URL url = new URL(serverUrl);
            URLConnection urlconn = url.openConnection();
            urlconn.setDoOutput(true);
            urlconn.setConnectTimeout(CONNECT_TIMEOUT);
            urlconn.setReadTimeout(READ_TIMEOUT);
            urlconn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.1) Gecko/20061204 Firefox/2.0.0.3");
            urlconn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            urlconn.setRequestProperty("Connection", "close");
            OutputStream out = null;
            if (ObjectUtils.allNotNull(body)) {
                out = urlconn.getOutputStream();
                out.write(body.getBytes(REQ_CHARSET));
                out.flush();
                out.close();
            }
            ByteArrayOutputStream respData = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            InputStream pis = urlconn.getInputStream();
            int len = 0;
            try {
                while (true) {
                    len = pis.read(b);
                    if (len <= 0) {
                        pis.close();
                        break;
                    }
                    respData.write(b, 0, len);
                }
            } catch (SocketTimeoutException ee) {
                log.error(ee.getMessage());
            } finally {
                if (null != out) {
                    out.close();
                }
                if (null != pis) {
                    pis.close();
                }
            }
            return respData.toString(RES_PCHARSET);
        } catch (MalformedURLException e) {
            log.warn("MalformedURLException: " + e.getMessage());
        } catch (IOException e) {
            if (e instanceof SocketTimeoutException) {
                log.warn(e.getMessage());
            }
            log.error("check here: " + e.toString());
        } catch (Exception e) {
            log.warn("other exception");
        }
        return null;
    }

    public static String get(String serverUrl) {
        return get(serverUrl, "");
    }

    /**
     * 通过http方式获取返回的数据
     *
     * @param urlStr      请求地址
     * @param requestData 请求数据
     * @param type        类型 form:表单 xml:application/xml等
     * @return
     * @throws Exception
     */
    public static String post(String urlStr, String requestData, String type) throws IOException {
        return post(urlStr, requestData, type, "", "", REQ_CHARSET, RES_PCHARSET, CONNECT_TIMEOUT, READ_TIMEOUT);
    }

    public static String post(String urlStr, String requestData, String type, String reqcharset, String respcharset)
            throws IOException {
        return post(urlStr, requestData, type, "", "", reqcharset, respcharset, CONNECT_TIMEOUT, READ_TIMEOUT);
    }

    /**
     * 通过http方式获取返回的数据
     *
     * @param urlStr         请求地址
     * @param requestData    请求数据
     * @param connectTimeOut 超时时长
     * @param timeOut
     * @param type           数据请求提交类型
     * @param charSet        字符集编码
     * @return
     * @throws IOException
     */
    public static String post(String urlStr, String requestData, int connectTimeOut, int timeOut, String type,
                              String charSet) throws IOException {
        return post(urlStr, requestData, type, "", "", charSet, charSet, connectTimeOut, timeOut);
    }

    public static String post(String urlStr, String requestData, String type, String charset) throws IOException {
        return post(urlStr, requestData, type, "", "", charset, charset, CONNECT_TIMEOUT, READ_TIMEOUT);
    }

    public static String post(String urlStr, String requestData, String type, String charset, String pfxFile,
                              String pwxPWD) throws IOException {
        return post(urlStr, requestData, type, pfxFile, pwxPWD, charset, charset, CONNECT_TIMEOUT, READ_TIMEOUT);
    }

    public static String post(String urlStr, String requestData, String type, String pfxFile, String pwxPWD,
                              String reqcharset, String respcharset, int connectTimeOut, int timeOut) throws IOException {
        URLConnection conn = null;
        OutputStream out = null;
        InputStream in = null;
        ByteArrayOutputStream byteArray = null;
        String temp = null;
        try {
            if (urlStr.startsWith("https") || urlStr.startsWith("HTTPS")) {
                SSLContext sslContext = null;
                sslContext = SSLContext.getInstance("TLS");
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                KeyStore ks = null;
                if (pfxFile.endsWith("jks") || pfxFile.endsWith("JKS")) {
                    ks = KeyStore.getInstance("JKS");
                } else {
                    ks = KeyStore.getInstance("PKCS12");
                }
                X509TrustManager[] xtmArray = new X509TrustManager[]{xtm};
                if (StringUtil.isNotNull(pfxFile) && StringUtil.isNotNull(pwxPWD)) {
                    ks.load(new FileInputStream(pfxFile), pwxPWD.toCharArray());
                    kmf.init(ks, pwxPWD.toCharArray());
                    sslContext.init(kmf.getKeyManagers(), xtmArray, null);
                } else {
                    sslContext.init(null, xtmArray, new java.security.SecureRandom());
                }
                HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier(hnv);
                HttpsURLConnection httpsUrlConn = (HttpsURLConnection) (new URL(urlStr)).openConnection();
                httpsUrlConn.setRequestMethod("POST");
                conn = httpsUrlConn;
            } else {
                URL url = new URL(urlStr);
                conn = url.openConnection();
            }
            // 设定请求方式为POST
            // 一定要设为true,因为要发送数据
            conn.setDoOutput(true);
            // 下面开始设定Http头
            if ("xml".equalsIgnoreCase(type)) {
                conn.setRequestProperty("Content-Length", requestData.length() + "");
                conn.setRequestProperty("Content-Type", "application/xml;charset=" + reqcharset);
            } else if ("json".equalsIgnoreCase(type)) {
                conn.setRequestProperty("Content-Length", requestData.length() + "");
                conn.setRequestProperty("Content-Type", "application/json;charset=" + reqcharset);
            } else {
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + reqcharset);
            }
            conn.setRequestProperty("Cache-Control", "no-bankcard");
            conn.setRequestProperty("Connection", "close");
            conn.setConnectTimeout(connectTimeOut);
            conn.setReadTimeout(timeOut);
            // 传送送据
            out = conn.getOutputStream();
            out.write(requestData.getBytes(reqcharset));
            out.flush();
            // 接收数据
            in = conn.getInputStream();
            byteArray = new ByteArrayOutputStream(in.available());
            byte[] b = new byte[8192];
            int len = -1;
            while ((len = in.read(b)) != -1) {
                byteArray.write(b, 0, len);
            }
            temp = byteArray.toString(respcharset);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            if (null != byteArray) {
                byteArray.close();
            }
            if (null != in) {
                in.close();
            }
            if (null != out) {
                out.close();
            }
            conn = null;
        }
        return temp;
    }

}

class MyX509TrustManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) {
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }
}

class MyHostnameVerifier implements HostnameVerifier {

    @Override
    public boolean verify(String hostname, SSLSession session) {
        return true;
    }

}
