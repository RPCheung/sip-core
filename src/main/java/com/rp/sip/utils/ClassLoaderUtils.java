package com.rp.sip.utils;

import com.rp.sip.classloader.SIPUserClassLoader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cheungrp on 18/2/2.
 */
public enum ClassLoaderUtils {

    utils;

    private Logger loggerMsg = LogManager.getLogger("com.rp.sip.SipMsg");
    private Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());


    public SIPUserClassLoader newSipUserClassloader(String classLoaderId) {
        BeanDefinitionBuilder builder = SpringBeanUtils.UTILS.addSpringBeanDefinition(SIPUserClassLoader.class);
        builder.addConstructorArgReference("commonClassLoader");
        SpringBeanUtils.UTILS.registerSpringBeanDefinition(builder, classLoaderId);
        return (SIPUserClassLoader) SpringBeanUtils.UTILS.getSpringBeanById(classLoaderId);
    }

    public Object createSipUserObject(String clazz) throws ClassNotFoundException {
        try {
            Class clz = SpringBeanUtils.UTILS.getSpringBeanById("sipUserClassLoader", SIPUserClassLoader.class).loadClass(clazz);
            return clz.newInstance();
        } catch (InstantiationException e) {
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
            CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, e);
            return null;
        } catch (IllegalAccessException e) {
            logger.error("必须提供一个无参的构造器!!!!");
            loggerMsg.error("必须提供一个无参的构造器!!!!");
            return null;
        }
    }

    public Object createSipUserObject(SIPUserClassLoader classloader, String clazz) throws ClassNotFoundException {
        Class clz = classloader.loadClass(clazz);
        try {
            return clz.newInstance();
        } catch (InstantiationException e) {
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
            CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, e);
            return null;
        } catch (IllegalAccessException e) {
            logger.error("必须提供一个无参的构造器!!!!");
            loggerMsg.error("必须提供一个无参的构造器!!!!");
            return null;
        }
    }

    public Class createSipUserClass(String clazz) throws ClassNotFoundException {
        Class clz = SpringBeanUtils.UTILS.getSpringBeanById("sipUserClassLoader", SIPUserClassLoader.class).loadClass(clazz);
        return clz;
    }

    public Class createSipUserClass(SIPUserClassLoader classloader, String clazz) throws ClassNotFoundException {
        Class clz = classloader.loadClass(clazz);
        return clz;
    }

    public SIPUserClassLoader getSipUserClassloader(String classLoaderId) {
        return SpringBeanUtils.UTILS.getSpringBeanById(classLoaderId, SIPUserClassLoader.class);
    }

    public SIPUserClassLoader getSipUserClassloader() {
        return SpringBeanUtils.UTILS.getSpringBeanById("sipUserClassLoader", SIPUserClassLoader.class);
    }

    public List<File> findJarInDirs(String dirPath) {
        return new ArrayList<>(FileUtils.listFiles(
                new File(dirPath), new String[]{"jar"}, true));
    }

    public List<File> findClassesInDirs(String rootDirPath) {
        return new ArrayList<>(FileUtils.listFiles(
                new File(rootDirPath), new String[]{"class"}, true));
    }

    public void moveDeployableJarInDirs(String srcPath, String deployablePath) throws IOException {
        File srcFile = FileUtils.getFile(srcPath);
        if (srcFile.isDirectory()) {
            List<File> files = new ArrayList<>(FileUtils.listFiles(srcFile, new String[]{"jar"}, false));
            if (files.size() == 0) {
                return;
            }
            for (File file : files) {
                FileUtils.moveFileToDirectory(file, new File(deployablePath), false);
            }
        } else {
            FileUtils.moveFileToDirectory(srcFile, new File(deployablePath), false);
        }
    }

    public void extractClassesFromJar(String jarPath, String desRootClassesDir) throws IOException, InterruptedException {
        File file = FileUtils.getFile(jarPath);
        if (!file.exists()) {
            return;
        }
        String fileName = file.getName();
        String dirName = fileName.substring(0, fileName.length() - 4);
        File desDir = new File(desRootClassesDir, dirName);
        FileUtils.moveFileToDirectory(new File(jarPath), desDir, true);

        Process process = Runtime.getRuntime().exec(SIPPath.JDK_COMMAND_PATH + File.separator + "jar xf " +
                desDir.getAbsolutePath() + File.separator + fileName, null, desDir);
        process.waitFor();
        File backupFile = FileUtils.getFile(SIPPath.BIZ_BACKUPS_PATH + File.separator + fileName);
        // 如果存在 同名备份包 就将旧包删除 再备份新包
        if (backupFile.exists()) {
            backupFile.delete();
        }
        FileUtils.moveFileToDirectory(FileUtils.getFile(desDir.getAbsolutePath() + File.separator + fileName), new File(SIPPath.BIZ_BACKUPS_PATH), false);

    }


}
