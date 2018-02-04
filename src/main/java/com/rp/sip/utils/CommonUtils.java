package com.rp.sip.utils;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.invoke.MethodHandles;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by RP on 2017/1/12.
 */
public class CommonUtils {

    private CommonUtils() {
    }

    private static class Instance {
        public static CommonUtils instance = new CommonUtils();
    }

    private Logger loggerMsg = LogManager.getLogger("com.rp.sip.SipMsg");
    private Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    public static CommonUtils getCommonUtils() {
        return Instance.instance;
    }

    public String getCurrentDate() {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    public String createLogDirectory() {

        String currentFilePath = System.getProperty("user.dir"); //获取可运行jar包的当前路径
        File currentFile = new File(currentFilePath + File.separator + "logs");

        if (currentFile.isDirectory() && currentFile.exists()) {
            return currentFile.getAbsolutePath();
        } else {
            currentFile.mkdirs();
            return currentFile.getAbsolutePath();
        }
    }

    public String getHost() {

        Enumeration<NetworkInterface> allNetInterfaces;
        try {
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress ip = addresses.nextElement();
                    if (ip != null && ip instanceof Inet4Address) {
                        logger.info("本机的IP = " + ip.getHostAddress());
                        return ip.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            printExceptionFormat(logger, e);
        }
        return null;
    }

    public String printExceptionFormat(Logger logger, Throwable throwable) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        throwable.printStackTrace(printStream);
        String errMsg = new String(outputStream.toByteArray());
        logger.error(":" + errMsg);
        return errMsg;
    }


    public List<File> findJarInDirs(String dirPath) {
        return new ArrayList<>(FileUtils.listFiles(
                new File(dirPath), new String[]{"jar"}, true));
    }

    public List<URL> findJarUrlInDirs(String dirPath) throws MalformedURLException {
        List<File> fileList = findJarInDirs(dirPath);
        List<URL> urls = new ArrayList<>(64);
        for (File file : fileList) {
            urls.add(new URL("file:" + file.getAbsolutePath()));
        }
        return urls;
    }


}
