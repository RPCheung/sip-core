package com.rp.sip.utils;

import com.rp.sip.classloader.SipUserClassloader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;

/**
 * Created by cheungrp on 18/2/2.
 */
public enum ClassLoadUtils {

    utils;

    private Logger loggerMsg = LogManager.getLogger("com.rp.sip.SipMsg");
    private Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());


    public Object createSipUserObject(String clazz) throws ClassNotFoundException {
        SipUserClassloader classloader = SpringBeanUtils.UTILS.getSpringBeanByType(SipUserClassloader.class);
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
        SipUserClassloader classloader = SpringBeanUtils.UTILS.getSpringBeanByType(SipUserClassloader.class);
        Class clz = classloader.loadClass(clazz);
        return clz;
    }

    public SipUserClassloader getSipUserClassloader() {
        return SpringBeanUtils.UTILS.getSpringBeanByType(SipUserClassloader.class);
    }
}
