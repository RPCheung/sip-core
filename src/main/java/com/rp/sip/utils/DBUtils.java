package com.rp.sip.utils;

import com.alibaba.druid.filter.config.ConfigTools;
import com.alibaba.druid.pool.DruidDataSource;
import com.mysql.jdbc.interceptors.SessionAssociationInterceptor;
import com.rp.sip.db.mapper.SipSettingDAO;
import com.rp.sip.db.mapper.UserDbDAO;
import com.rp.sip.model.SIPInfo;
import org.apache.ibatis.io.ResolverUtil;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by cheungrp on 18/1/15.
 */
public enum DBUtils {

    UTILS;

    private Logger loggerMsg = LogManager.getLogger("com.rp.sip.SipMsg");
    private Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    DBUtils() {
    }


    public void createUserDataSourceAndSessionFactory() {
        BeanDefinitionBuilder dataSourceBuilder = null;
        try {
            dataSourceBuilder = BeanDefinitionBuilder
                    .genericBeanDefinition(ClassLoaderUtils.utils.getSipUserClassloader()
                            .loadClass("com.alibaba.druid.pool.DruidDataSource"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
        }
        dataSourceBuilder
                .addPropertyValue("username", queryDbSetting().get("username"))
                .addPropertyValue("password", decryptDbPassword((String) queryDbSetting().get("password")))
                .addPropertyValue("dbType", queryDbSetting().get("dbType"))
                .addPropertyValue("initialSize", queryDbSetting().get("initialSize"))
                .addPropertyValue("maxActive", queryDbSetting().get("maxActive"))
                .addPropertyValue("minIdle", queryDbSetting().get("minIdle"))
                .addPropertyValue("maxWait", queryDbSetting().get("maxWait"))
                .addPropertyValue("url", queryDbSetting().get("url"))
                .addPropertyValue("driverClassName", queryDbSetting().get("driverClassName"))
                .addPropertyValue("testWhileIdle", queryDbSetting().get("testWhileIdle"))
                .addPropertyReference("proxyFilters", "filters");

        ((DefaultListableBeanFactory) SpringBeanFactory.getBeanFactory()).registerBeanDefinition("userDataSource",
                dataSourceBuilder.getRawBeanDefinition());

        DruidDataSource dataSource = (DruidDataSource) SpringBeanUtils.UTILS.getSpringBeanById("userDataSource");
        try {
            dataSource.init();
        } catch (SQLException e) {
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
            CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, e);
        }
        BeanDefinitionBuilder sessionFactoryBuilder = null;
        try {
            sessionFactoryBuilder = BeanDefinitionBuilder
                    .genericBeanDefinition(ClassLoaderUtils.utils.getSipUserClassloader()
                            .loadClass("org.mybatis.spring.SqlSessionFactoryBean"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        sessionFactoryBuilder.addPropertyReference("dataSource", "userDataSource");
        ((DefaultListableBeanFactory) SpringBeanFactory.getBeanFactory()).registerBeanDefinition("userSqlSessionFactory",
                sessionFactoryBuilder.getRawBeanDefinition());
        SqlSessionFactory sessionFactory = (SqlSessionFactory) SpringBeanUtils.UTILS.getSpringBeanById("userSqlSessionFactory");

    }

    public void autoCreateUserDataSourceAndSessionFactory() {
        boolean isUserDB = Boolean.valueOf((String) queryDbSetting().get("isUserDB"));
        if (!isUserDB) {
            return;
        }
        createUserDataSourceAndSessionFactory();
    }

    public DruidDataSource getUserDataSource() {
        return (DruidDataSource) SpringBeanUtils.UTILS.getSpringBeanById("userDataSource");
    }

    public SqlSessionFactory getUserSqlSessionFactory() {
        return SqlSessionFactory.class.cast(SpringBeanUtils.UTILS.getSpringBeanById("userSqlSessionFactory"));

    }

    public <T> T getUserSQLMapper(SqlSession session, Class<T> clz) {
        Configuration configuration = session.getConfiguration();
        if (!configuration.getMapperRegistry().hasMapper(clz)) {
            configuration.addMapper(clz);
        }
        T t = session.getMapper(clz);
        return t;
    }

    public <T> Map<Class<T>, T> getUserSQLMapper(SqlSession session, String packageName) {
        Configuration configuration = session.getConfiguration();
        ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<>();
        resolverUtil.find(new ResolverUtil.IsA(Object.class), packageName);
        Set<Class<? extends Class<?>>> mapperSet = resolverUtil.getClasses();
        for (Class<?> mapperClass : mapperSet) {
            if (!configuration.hasMapper(mapperClass)) {
                configuration.addMapper(mapperClass);
            }
        }


        List<Class> mapperClasses = new ArrayList<>(configuration.getMapperRegistry().getMappers());
        Map<Class<T>, T> mapperedClasses = new ConcurrentHashMap<>(mapperClasses.size());
        for (Class<T> clazz : mapperClasses) {
            mapperedClasses.put(clazz, session.getMapper(clazz));
        }
        return mapperedClasses;
    }

    public String encryptDbPassword(String plainText) {
        try {
            return ConfigTools.encrypt(plainText);
        } catch (Exception e) {
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
            CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, e);
            return null;
        }
    }

    public String encryptDbPassword(String privateKeyText, String plainText) {
        try {
            return ConfigTools.encrypt(privateKeyText, plainText);
        } catch (Exception e) {
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
            CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, e);
            return null;
        }
    }

    public String decryptDbPassword(String cipherText) {
        try {
            return ConfigTools.decrypt(cipherText);
        } catch (Exception e) {
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
            CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, e);
            return null;
        }
    }

    public String decryptDbPassword(String publicKeyText, String cipherText) {
        try {
            return ConfigTools.decrypt(publicKeyText, cipherText);
        } catch (Exception e) {
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
            CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, e);
            return null;
        }
    }

    private Map<String, Object> queryDbSetting() {
        UserDbDAO userDbDAO = SpringBeanUtils.UTILS.getSpringBeanByType(UserDbDAO.class);
        SipSettingDAO settingDAO = SpringBeanUtils.UTILS.getSpringBeanByType(SipSettingDAO.class);
        SIPInfo info = (SIPInfo) SpringBeanUtils.UTILS.getSpringBeanById("sip-info");
        String host = (String) settingDAO.querySetting(info.getServerId()).get("host");
        return userDbDAO.queryDbSetting(host);
    }
}
