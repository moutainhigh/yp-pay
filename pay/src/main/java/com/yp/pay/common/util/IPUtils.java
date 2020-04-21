//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.yp.pay.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IPUtils {
    private static Logger logger = LoggerFactory.getLogger(IPUtils.class);
    private static final String GET_LOCAL_IP_CMD = "/sbin/ifconfig -a|grep inet|grep -v 127.0.0.1|grep -v 0.0.0.0|grep -v inet6|awk '{print $2}'|tr -d addr:";

    public IPUtils() {
    }

    public static String getIpAddr(HttpServletRequest request) {
        String ip = null;

        try {
            ip = request.getHeader("x-forwarded-for");
            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }

            if (StringUtils.isEmpty(ip) || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Real-IP");
            }

            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }

            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }

            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }

            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
        } catch (Exception var3) {
            logger.error("com.jw.foundation.common.IPUtils ERROR ", var3);
        }

        return ip;
    }

    public static String getLocalIp() {
        InetAddress address = null;

        try {
            if (isWindowsOS()) {
                address = InetAddress.getLocalHost();
            } else {
                String linuxLocalIp = getLinuxLocalIp();
                if (StringUtils.isNotBlank(linuxLocalIp)) {
                    logger.info("通过shell命令获取linux本地ip成功：{}", linuxLocalIp);
                    return linuxLocalIp;
                }

                boolean findIP = false;
                Enumeration netInterfaces = NetworkInterface.getNetworkInterfaces();

                label48:
                while(true) {
                    while(true) {
                        if (!netInterfaces.hasMoreElements() || findIP) {
                            break label48;
                        }

                        NetworkInterface ni = (NetworkInterface)netInterfaces.nextElement();
                        Enumeration addresses = ni.getInetAddresses();

                        while(addresses.hasMoreElements()) {
                            address = (InetAddress)addresses.nextElement();
                            if (address.isSiteLocalAddress() && !address.isLoopbackAddress() && !address.getHostAddress().contains(":")) {
                                findIP = true;
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception var6) {
            throw new RuntimeException("获取局域网ip失败:" + var6.getMessage());
        }

        if (null == address) {
            throw new RuntimeException("获取局域网ip失败:InetAddress == null");
        } else {
            logger.info("方式二获取linux本地ip成功：{}", address.getHostAddress());
            return address.getHostAddress();
        }
    }

    public static Long ipToLong(String ipStr) {
        Long ip = 0L;
        String[] numbers = ipStr.split("\\.");

        for(int i = 0; i < numbers.length; ++i) {
            ip = ip << 8 | (long)Integer.parseInt(numbers[i]);
        }

        return ip;
    }

    public static boolean isWindowsOS() {
        return System.getProperty("os.name").toLowerCase().indexOf("windows") > -1;
    }

    private static String getLinuxLocalIp() {
        try {
            Process proc = Runtime.getRuntime().exec(new String[]{"sh", "-c", "/sbin/ifconfig -a|grep inet|grep -v 127.0.0.1|grep -v 0.0.0.0|grep -v inet6|awk '{print $2}'|tr -d addr:"});
            proc.waitFor();
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String temp;
            if ((temp = br.readLine()) != null) {
            }

            br.close();
            return temp;
        } catch (IOException var3) {
            throw new RuntimeException("获取linux本地ip出错：IOException", var3);
        } catch (InterruptedException var4) {
            throw new RuntimeException("获取linux本地ip出错：InterruptedException", var4);
        }
    }
}
