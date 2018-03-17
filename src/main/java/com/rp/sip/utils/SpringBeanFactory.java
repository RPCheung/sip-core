package com.rp.sip.utils;


import com.rp.sip.classloader.SIPUserClassLoader;
import com.rp.sip.db.mapper.CustomComponentDAO;
import com.rp.sip.db.mapper.SipSettingDAO;
import com.rp.sip.model.SIPInfo;
import com.rp.sip.quartz.DeadlockChecker;
import com.rp.sip.route.pool.SipFixedChannelPool;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.SchedulerException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.rp.sip.utils.SIPPath.*;

/**
 * Created by cheungrp on 17/7/14.
 */
@Component
public class SpringBeanFactory implements ApplicationContextAware, BeanFactoryAware, ApplicationListener {

    private static ApplicationContext applicationContext = null;
    private static BeanFactory beanFactory = null;

    private Logger loggerMsg = LogManager.getLogger("com.rp.sip.SipMsg");
    private Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    @Value("${server-id}")
    String serverId;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringBeanFactory.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        SpringBeanFactory.beanFactory = beanFactory;
    }

    public static BeanFactory getBeanFactory() {
        return beanFactory;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {

            // check server
            checkServerId();

            // SipUserClassloader load libs
            loadAllUserJar();

            // userDB init
            DBUtils.UTILS.autoCreateUserDataSourceAndSessionFactory();

            // init sip custom component
            initCustomComponent();

            // init sip route
            initRouteAllComponent();

            // init deadlockChecker
            initDeadlockChecker();
        }
    }

    private void initDeadlockChecker() {
        DeadlockChecker deadlockChecker = (DeadlockChecker) SpringBeanUtils.UTILS.getSpringBeanById("deadlockChecker");
        try {
            deadlockChecker.init();
        } catch (SchedulerException e) {
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
            CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, e);
        }
    }

    private void checkServerId() {
        SIPInfo info = (SIPInfo) SpringBeanUtils.UTILS.getSpringBeanById("sip-info");
        String serverId = info.getServerId();
        SipSettingDAO settingDAO = SpringBeanUtils.UTILS.getSpringBeanByType(SipSettingDAO.class);
        if (settingDAO.querySetting(serverId) == null) {
            throw new IllegalArgumentException("找不到此 serverId 的设置信息 !!!");
        }
    }

    private void initCustomComponent() {
        // initMessageInterceptor();
        initBusinessDispatcherHandler();
        initFindTxCodeHandler();
        initTransactionMappingHandler();
        initPackMessage();
    }

    private void initRouteAllComponent() {
        // initRoute();
        initRouteChannelPool();
    }

    private void initRouteChannelPool() {
        SipFixedChannelPool routeChannelPool = (SipFixedChannelPool) SpringBeanUtils.UTILS.getSpringBeanById("routeChannelPool");
        routeChannelPool.init();

    }

    private void loadAllUserJar() {
        ClassLoaderUtils.utils.newSipUserClassloader("sipUserClassLoader");
        loadUserJar(SYSTEM_PATH);
        loadUserJar(JAR_PATH);
        loadUserBiz(DEPLOYABLE_PATH);
    }

    private void loadUserBiz(String DEPLOYABLE_PATH) {
        SIPUserClassLoader classloader = SpringBeanFactory.applicationContext.getBean(SIPUserClassLoader.class);
        try {
            ClassLoaderUtils.utils.moveDeployableJarInDirs(DEPLOYABLE_PATH, SIPPath.BIZ_PATH);
        } catch (IOException e) {
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
            CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, e);
        }
        List<File> files = new ArrayList<>(FileUtils.listFiles(
                new File(SIPPath.BIZ_PATH), new String[]{"jar"}, true));
        if (files.size() == 0) {
            // 没有新的部署包时  在 BIZ_BACKUPS_PATH 上 寻找是否存在已加载过的部署包
            List<File> backupsFiles = new ArrayList<>(FileUtils.listFiles(
                    new File(SIPPath.BIZ_BACKUPS_PATH), new String[]{"jar"}, true));
            if (backupsFiles.size() == 0) {
                return;
            }
            // 有则进行加载
            for (File file : backupsFiles) {
                String fileName = file.getName();
                String desDir = fileName.substring(0, fileName.length() - 4);
                String classesDirPath = SIPPath.BIZ_PATH + File.separator + desDir + File.separator;
                try {
                    classloader.addURL(classesDirPath);
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    logger.info("本次启动无新部署包,已加载 {} 的部署包,名为:{}",
                            format.format(new Date(file.lastModified())), file.getName());
                    loggerMsg.info("本次启动无新部署包,已加载 {} 的部署包,名为:{}",
                            format.format(new Date(file.lastModified())), file.getName());
                } catch (MalformedURLException e) {
                    CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
                    CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, e);
                }
            }
            return;
        }
        for (File file : files) {
            try {
                String fileName = file.getName();
                String desDir = fileName.substring(0, fileName.length() - 4);
                String classesDirPath = SIPPath.BIZ_PATH + File.separator + desDir + File.separator;
                // 加载时 先 检查 是否已有同名部署包 有则删除 再把新包部署
                if (FileUtils.getFile(classesDirPath).exists()) {
                    FileUtils.deleteDirectory(FileUtils.getFile(classesDirPath));
                    logger.info("存在同名部署包,正在将旧部署包删除: {}", !FileUtils.getFile(classesDirPath).exists());
                    loggerMsg.info("存在同名部署包,正在将旧部署包删除: {}", !FileUtils.getFile(classesDirPath).exists());
                }
                ClassLoaderUtils.utils
                        .extractClassesFromJar(file.getAbsolutePath(), SIPPath.BIZ_PATH);
                classloader.addURL(classesDirPath);
                logger.info("已加载最新部署包: {}", file.getName());
                loggerMsg.info("已加载最新部署包: {}", file.getName());
            } catch (IOException | InterruptedException e) {
                CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
                CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, e);
            }
        }
    }

    private void loadUserJar(String JAR_PATH) {
        SIPUserClassLoader classloader = SpringBeanFactory.applicationContext.getBean(SIPUserClassLoader.class);
        List<File> files = new ArrayList<>(FileUtils.listFiles(
                new File(JAR_PATH), new String[]{"jar"}, true));
        if (files.size() == 0) {
            return;
        }
        for (File file : files) {
            try {
                classloader.addURL(file.getAbsolutePath());
            } catch (MalformedURLException e) {
                CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
                CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, e);
            }
        }
    }

    private void initMessageInterceptor() {
        try {
            CustomComponentDAO customComponentDAO = SpringBeanUtils.UTILS.getSpringBeanByType(CustomComponentDAO.class);
            Map<String, Object> result = customComponentDAO.queryDbCustom();
            String messageInterceptor = (String) result.get("messageInterceptor");
            if (messageInterceptor == null || messageInterceptor.equals("")) {
                return;
            }
            BeanDefinitionBuilder builder = SpringBeanUtils.UTILS
                    .addSpringBeanDefinition(ClassLoaderUtils.utils
                            .getSipUserClassloader().loadClass(messageInterceptor));
            SpringBeanUtils.UTILS.registerSpringBeanDefinition(builder, "messageInterceptor");
            Object handler = SpringBeanUtils.UTILS.getSpringBeanById("messageInterceptor");
            if (handler == null) {
                throw new RuntimeException("加载 handler 出错!!!");
            }
            logger.info("配置了 MessageInterceptor :" + handler.getClass().getName());
            loggerMsg.info("配置了 MessageInterceptor :" + handler.getClass().getName());
        } catch (ClassNotFoundException e) {
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
            CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, e);
        }
    }

    private void initBusinessDispatcherHandler() {
        try {
            CustomComponentDAO customComponentDAO = SpringBeanUtils.UTILS.getSpringBeanByType(CustomComponentDAO.class);
            Map<String, Object> result = customComponentDAO.queryDbCustom();
            String messageInterceptor = (String) result.get("businessDispatcherHandler");
            if (messageInterceptor == null || messageInterceptor.equals("")) {
                return;
            }
            BeanDefinitionBuilder builder = SpringBeanUtils.UTILS
                    .addSpringBeanDefinition(ClassLoaderUtils.utils
                            .getSipUserClassloader().loadClass(messageInterceptor));
            SpringBeanUtils.UTILS.registerSpringBeanDefinition(builder, "customBusinessDispatcherHandler");
            Object handler = SpringBeanUtils.UTILS.getSpringBeanById("customBusinessDispatcherHandler");
            if (handler == null) {
                throw new RuntimeException("加载 handler 出错!!!");
            }
            logger.info("配置了 BusinessDispatcherHandler :" + handler.getClass().getName());
            loggerMsg.info("配置了 BusinessDispatcherHandler :" + handler.getClass().getName());
        } catch (ClassNotFoundException e) {
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
            CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, e);
        }
    }

    private void initFindTxCodeHandler() {
        try {
            CustomComponentDAO customComponentDAO = SpringBeanUtils.UTILS.getSpringBeanByType(CustomComponentDAO.class);
            Map<String, Object> result = customComponentDAO.queryDbCustom();
            String messageInterceptor = (String) result.get("findTxCodeHandler");
            if (messageInterceptor == null || messageInterceptor.equals("")) {
                return;
            }
            BeanDefinitionBuilder builder = SpringBeanUtils.UTILS
                    .addSpringBeanDefinition(ClassLoaderUtils.utils
                            .getSipUserClassloader().loadClass(messageInterceptor));
            SpringBeanUtils.UTILS.registerSpringBeanDefinition(builder, "customFindTxCodeHandler");
            Object handler = SpringBeanUtils.UTILS.getSpringBeanById("customFindTxCodeHandler");
            if (handler == null) {
                throw new RuntimeException("加载 handler 出错!!!");
            }
            logger.info("配置了 FindTxCodeHandler :" + handler.getClass().getName());
            loggerMsg.info("配置了 FindTxCodeHandler :" + handler.getClass().getName());
        } catch (ClassNotFoundException e) {
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
            CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, e);
        }
    }

    private void initTransactionMappingHandler() {
        try {
            CustomComponentDAO customComponentDAO = SpringBeanUtils.UTILS.getSpringBeanByType(CustomComponentDAO.class);
            Map<String, Object> result = customComponentDAO.queryDbCustom();
            String messageInterceptor = (String) result.get("transactionMappingHandler");
            if (messageInterceptor == null || messageInterceptor.equals("")) {
                return;
            }
            BeanDefinitionBuilder builder = SpringBeanUtils.UTILS
                    .addSpringBeanDefinition(ClassLoaderUtils.utils
                            .getSipUserClassloader().loadClass(messageInterceptor));
            SpringBeanUtils.UTILS.registerSpringBeanDefinition(builder, "customTransactionMappingHandler");
            Object handler = SpringBeanUtils.UTILS.getSpringBeanById("customTransactionMappingHandler");
            if (handler == null) {
                throw new RuntimeException("加载 handler 出错!!!");
            }
            logger.info("配置了 TransactionMappingHandler :" + handler.getClass().getName());
            loggerMsg.info("配置了 TransactionMappingHandler :" + handler.getClass().getName());
        } catch (ClassNotFoundException e) {
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
            CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, e);
        }
    }

    private void initPackMessage() {
        try {
            CustomComponentDAO customComponentDAO = SpringBeanUtils.UTILS.getSpringBeanByType(CustomComponentDAO.class);
            Map<String, Object> result = customComponentDAO.queryDbCustom();
            String messageInterceptor = (String) result.get("packMessage");
            if (messageInterceptor == null || messageInterceptor.equals("")) {
                return;
            }
            BeanDefinitionBuilder builder = SpringBeanUtils.UTILS
                    .addSpringBeanDefinition(ClassLoaderUtils.utils
                            .getSipUserClassloader().loadClass(messageInterceptor));
            SpringBeanUtils.UTILS.registerSpringBeanDefinition(builder, "customMessagePacker");
            Object handler = SpringBeanUtils.UTILS.getSpringBeanById("customMessagePacker");
            if (handler == null) {
                throw new RuntimeException("加载 PackMessage 出错!!!");
            }
            logger.info("配置了 PackMessage :" + handler.getClass().getName());
            loggerMsg.info("配置了 PackMessage :" + handler.getClass().getName());
        } catch (ClassNotFoundException e) {
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
            CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, e);
        }
    }

}
