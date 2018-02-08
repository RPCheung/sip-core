package com.rp.sip.utils;

import com.alibaba.druid.filter.config.ConfigTools;
import com.alibaba.druid.filter.logging.LogFilter;
import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.rp.sip.db.mapper.SipSettingDAO;
import com.rp.sip.db.mapper.UserDbDAO;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

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
                    .genericBeanDefinition(ClassLoadUtils.utils.getSipUserClassloader()
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
                    .genericBeanDefinition(ClassLoadUtils.utils.getSipUserClassloader()
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
        String host = (String) settingDAO.querySetting().get("host");
        return userDbDAO.queryDbSetting(host);
    }
}
