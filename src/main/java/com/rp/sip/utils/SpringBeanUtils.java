package com.rp.sip.utils;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.ClassUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by cheungrp on 18/1/3.
 */
public enum SpringBeanUtils {
    UTILS;

    SpringBeanUtils() {
    }

    public boolean isContainsBean4Id(String id) {
        return SpringBeanFactory.getApplicationContext().containsBean(id);
    }

    @Deprecated
    public Object getSpringBeanById(String id) {
        if (SpringBeanFactory.getApplicationContext().containsBean(id)) {
            return SpringBeanFactory.getApplicationContext().getBean(id);
        }
        return null;
    }

    public <Type> Type getSpringBeanByType(Class<Type> clz) {
        return SpringBeanFactory.getApplicationContext().getBean(clz);
    }

    public <Type> Type getSpringBeanById(String id, Class<Type> clz) {
        if (SpringBeanFactory.getApplicationContext().containsBean(id)) {
            return clz.cast(SpringBeanFactory.getApplicationContext().getBean(id));
        }
        return null;
    }

    public Object getBeanWithAnnotationById(Class clazz, String id) {

        Map<String, Object> beans = SpringBeanFactory.getApplicationContext().getBeansWithAnnotation(clazz);
        if (beans != null) {
            return beans.get(id);
        }
        return null;
    }

    public Object getBeanOfTypeById(Class clazz, String id) {
        Map<String, Object> beans = SpringBeanFactory.getApplicationContext().getBeansOfType(clazz);
        if (beans != null) {
            return beans.get(id);
        }
        return null;
    }

    @Deprecated
    public BeanDefinitionBuilder addSpringBeanDefinition(Class clz) {
        return BeanDefinitionBuilder.genericBeanDefinition(clz);
    }

    public BeanDefinitionBuilder addSpringBeanDefinition(String className) throws ClassNotFoundException {
        Class clz = Thread.currentThread().getContextClassLoader().loadClass(className);
        return BeanDefinitionBuilder.genericBeanDefinition(clz);
    }

    public BeanDefinitionBuilder addSpringBeanDefinition(Thread currentThread, String className) throws ClassNotFoundException {
        Class clz = currentThread.getContextClassLoader().loadClass(className);
        return BeanDefinitionBuilder.genericBeanDefinition(clz);
    }

    public BeanDefinitionBuilder addSpringBeanDefinitionFromUserClassLoader(String className) throws ClassNotFoundException {
        return BeanDefinitionBuilder.genericBeanDefinition(ClassLoadUtils.utils.getSipUserClassloader().loadClass(className));
    }


    public void registerSpringBeanDefinition(BeanDefinitionBuilder beanDefinitionBuilder, String beanId) {
        ((DefaultListableBeanFactory) SpringBeanFactory.getBeanFactory()).registerBeanDefinition(beanId,
                beanDefinitionBuilder.getRawBeanDefinition());
    }

    public void unregisterSpringBeanDefinition(String beanId) {
        ((DefaultListableBeanFactory) SpringBeanFactory.getBeanFactory()).removeBeanDefinition(beanId);
    }

    public void refreshContext() {
        ClassPathXmlApplicationContext context = (ClassPathXmlApplicationContext) SpringBeanFactory.getApplicationContext();
        context.refresh();
    }

    public void closeContext() {
        ClassPathXmlApplicationContext context = (ClassPathXmlApplicationContext) SpringBeanFactory.getApplicationContext();
        context.close();
    }
}
