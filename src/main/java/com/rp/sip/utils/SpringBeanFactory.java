package com.rp.sip.utils;


import com.alibaba.fastjson.JSONArray;
import com.rp.sip.classloader.SipUserClassloader;
import com.rp.sip.db.mapper.CustomDbDAO;
import com.rp.sip.handlers.BusinessDispatcherHandler;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.msgpack.template.builder.BuildContext;
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
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by cheungrp on 17/7/14.
 */
@Component
public class SpringBeanFactory implements ApplicationContextAware, BeanFactoryAware, ApplicationListener {

    private static ApplicationContext applicationContext = null;
    private static BeanFactory beanFactory = null;

    private Logger loggerMsg = LogManager.getLogger("com.rp.sip.SipMsg");
    private Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private static final String JAR_PATH = System.getenv("SIP_HOME") + "/work/user/lib";
    private static final String SYSTEM_PATH = System.getenv("SIP_HOME") + "/work/sip/lib";
    private static final String BIZ_PATH = System.getenv("SIP_HOME") + "/work/user";

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

            // SipUserClassloader load libs
            loadAllUserJar();

            // userDB init
            DBUtils.UTILS.autoCreateUserDataSourceAndSessionFactory();

            // init sip custom component
            initCustomComponent();
        }
    }

    private void initCustomComponent() {
        initMessageInterceptor();
        initBusinessDispatcherHandler();
        initFindTxCodeHandler();
        initTransactionMappingHandler();
        initPackMessage();
    }

    private void loadAllUserJar() {
        loadUserJar(SYSTEM_PATH);
        loadUserJar(JAR_PATH);
        loadUserJar(BIZ_PATH);
    }

    private void loadUserJar(String JAR_PATH) {
        SipUserClassloader classloader = SpringBeanFactory.applicationContext.getBean(SipUserClassloader.class);
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
            CustomDbDAO customDbDAO = SpringBeanUtils.UTILS.getSpringBeanByType(CustomDbDAO.class);
            Map<String, Object> result = customDbDAO.queryDbCustom();
            String messageInterceptor = (String) result.get("messageInterceptor");
            if (messageInterceptor == null || messageInterceptor.equals("")) {
                return;
            }
            BeanDefinitionBuilder builder = SpringBeanUtils.UTILS.addSpringBeanDefinitionFromUserClassLoader(messageInterceptor);
            SpringBeanUtils.UTILS.registerSpringBeanDefinition(builder, "messageInterceptor");
            Object handler = SpringBeanUtils.UTILS.getSpringBeanById("messageInterceptor");
            if (handler == null) {
                throw new RuntimeException("加载 handler 出错!!!");
            }
            logger.info("配置了 MessageInterceptor :" + handler.getClass().getName());
            loggerMsg.info("配置了 MessageInterceptor :" + handler.getClass().getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
            CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, e);
        }
    }

    private void initBusinessDispatcherHandler() {
        try {
            CustomDbDAO customDbDAO = SpringBeanUtils.UTILS.getSpringBeanByType(CustomDbDAO.class);
            Map<String, Object> result = customDbDAO.queryDbCustom();
            String messageInterceptor = (String) result.get("businessDispatcherHandler");
            if (messageInterceptor == null || messageInterceptor.equals("")) {
                return;
            }
            BeanDefinitionBuilder builder = SpringBeanUtils.UTILS.addSpringBeanDefinitionFromUserClassLoader(messageInterceptor);
            SpringBeanUtils.UTILS.registerSpringBeanDefinition(builder, "customBusinessDispatcherHandler");
            Object handler = SpringBeanUtils.UTILS.getSpringBeanById("customBusinessDispatcherHandler");
            if (handler == null) {
                throw new RuntimeException("加载 handler 出错!!!");
            }
            logger.info("配置了 BusinessDispatcherHandler :" + handler.getClass().getName());
            loggerMsg.info("配置了 BusinessDispatcherHandler :" + handler.getClass().getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
        }
    }

    private void initFindTxCodeHandler() {
        try {
            CustomDbDAO customDbDAO = SpringBeanUtils.UTILS.getSpringBeanByType(CustomDbDAO.class);
            Map<String, Object> result = customDbDAO.queryDbCustom();
            String messageInterceptor = (String) result.get("findTxCodeHandler");
            if (messageInterceptor == null || messageInterceptor.equals("")) {
                return;
            }
            BeanDefinitionBuilder builder = SpringBeanUtils.UTILS.addSpringBeanDefinitionFromUserClassLoader(messageInterceptor);
            SpringBeanUtils.UTILS.registerSpringBeanDefinition(builder, "customFindTxCodeHandler");
            Object handler = SpringBeanUtils.UTILS.getSpringBeanById("customFindTxCodeHandler");
            if (handler == null) {
                throw new RuntimeException("加载 handler 出错!!!");
            }
            logger.info("配置了 FindTxCodeHandler :" + handler.getClass().getName());
            loggerMsg.info("配置了 FindTxCodeHandler :" + handler.getClass().getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
        }
    }

    private void initTransactionMappingHandler() {
        try {
            CustomDbDAO customDbDAO = SpringBeanUtils.UTILS.getSpringBeanByType(CustomDbDAO.class);
            Map<String, Object> result = customDbDAO.queryDbCustom();
            String messageInterceptor = (String) result.get("transactionMappingHandler");
            if (messageInterceptor == null || messageInterceptor.equals("")) {
                return;
            }
            BeanDefinitionBuilder builder = SpringBeanUtils.UTILS.addSpringBeanDefinitionFromUserClassLoader(messageInterceptor);
            SpringBeanUtils.UTILS.registerSpringBeanDefinition(builder, "customTransactionMappingHandler");
            Object handler = SpringBeanUtils.UTILS.getSpringBeanById("customTransactionMappingHandler");
            if (handler == null) {
                throw new RuntimeException("加载 handler 出错!!!");
            }
            logger.info("配置了 TransactionMappingHandler :" + handler.getClass().getName());
            loggerMsg.info("配置了 TransactionMappingHandler :" + handler.getClass().getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
        }
    }

    private void initPackMessage() {
        try {
            CustomDbDAO customDbDAO = SpringBeanUtils.UTILS.getSpringBeanByType(CustomDbDAO.class);
            Map<String, Object> result = customDbDAO.queryDbCustom();
            String messageInterceptor = (String) result.get("packMessage");
            if (messageInterceptor == null || messageInterceptor.equals("")) {
                return;
            }
            BeanDefinitionBuilder builder = SpringBeanUtils.UTILS.addSpringBeanDefinitionFromUserClassLoader(messageInterceptor);
            SpringBeanUtils.UTILS.registerSpringBeanDefinition(builder, "customMessagePacker");
            Object handler = SpringBeanUtils.UTILS.getSpringBeanById("customMessagePacker");
            if (handler == null) {
                throw new RuntimeException("加载 PackMessage 出错!!!");
            }
            logger.info("配置了 PackMessage :" + handler.getClass().getName());
            loggerMsg.info("配置了 PackMessage :" + handler.getClass().getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
        }
    }

}
