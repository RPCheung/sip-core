package com.rp.sip.message;

import com.alibaba.fastjson.JSON;
import com.rp.sip.annotation.SipMessage;
import com.rp.sip.component.MessageObject;
import com.rp.sip.utils.CommonUtils;
import io.netty.buffer.ByteBuf;
import org.apache.commons.io.IOUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;

/**
 * Created by cheungrp on 17/11/3.
 */
public class DefaultMessageObject implements MessageObject {

    private Logger loggerMsg = LogManager.getLogger("com.rp.sip.SipMsg");
    private Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());


    private JXPathContext context;

    public DefaultMessageObject(JXPathContext context) throws IllegalArgumentException {

        this.context = context;
        this.context.setLenient(true);
        MessageBeanFactory factory = new MessageBeanFactory();
        this.context.setFactory(factory);
        if (this.context.getContextBean() == null) {
            throw new IllegalArgumentException(" message is null ");
        }
        Annotation[] annotations = this.context.getContextBean().getClass().getAnnotations();
        Annotation annotation = null;

        for (Annotation temp : annotations) {
            if ((annotation = temp.annotationType().getAnnotation(SipMessage.class)) != null) {
                break;
            }
        }

        if (annotation == null) {
            throw new IllegalArgumentException("the pojo is not with @*Message");
        }
    }

    @Override
    public Object getSipMessagePojo() {
        return this.context.getContextBean();
    }

    @Override
    public String toString(Charset charset) {
        try {
            return IOUtils.toString(JSON.toJSONBytes(getSipMessagePojo()), charset.toString());
        } catch (IOException e) {
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
            CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, e);
            return null;
        }
    }

    @Override
    public String toString() {
        return getSipMessagePojo().toString();
    }

    @Override
    public Object get(String key) {
        return getObjectValue(key);
    }

    @Override
    public void set(String key, Object value) {
        setObjectValue(key, value);
    }

    @Override
    public String getString(String key) {
        return checkValueIsNull(getObjectValue(key));
    }

    @Override
    public void setString(String key, String value) {
        setObjectValue(key, value);
    }

    // 以下暂不可用

    @Override
    public Integer getInteger(String key) {
        return Integer.valueOf(this.context.getValue(key).toString());
    }

    @Override
    public void setInteger(String key, Integer value) {
        this.context.setValue(key, value);
    }

    @Override
    public Long getLong(String key) {
        return Long.valueOf(this.context.getValue(key).toString());
    }

    @Override
    public void setLong(String key, Long value) {
        this.context.setValue(key, value);
    }

    @Override
    public Float getFloat(String key) {
        return Float.valueOf(this.context.getValue(key).toString());
    }

    @Override
    public void setFloat(String key, Float value) {
        this.context.setValue(key, value);
    }

    @Override
    public Double getDouble(String key) {
        return Double.valueOf(this.context.getValue(key).toString());
    }

    @Override
    public void setDouble(String key, Float value) {
        this.context.setValue(key, value);
    }

    @Override
    public Date getDate(String key) {
        return new Date(this.context.getValue(key).toString());
    }

    @Override
    public void setDate(String key, Date value) {
        this.context.setValue(key, value);
    }

    @Override
    public Boolean getBoolean(String key) {
        return Boolean.valueOf(this.context.getValue(key).toString());
    }

    @Override
    public void setBoolean(String key, Boolean value) {
        this.context.setValue(key, value);
    }

    @Override
    public byte[] getBytes(String key) {
        return byte[].class.cast(this.context.getValue(key));
    }

    @Override
    public void setBytes(String key, byte[] value) {
        this.context.setValue(key, value);
    }

    @Override
    public ByteBuf getByteBuf(String key) {
        return ByteBuf.class.cast(this.context.getValue(key));
    }

    @Override
    public void setByteBuf(String key, ByteBuf value) {
        this.context.setValue(key, value);
    }

    @Override
    public List getList(String key) {
        return List.class.cast(this.context.getValue(key));
    }

    @Override
    public void setList(String key, List value) {
        this.context.setValue(key, value);
    }

    @Override
    public MessageObject getMessageObject(String key) {
        return MessageObject.class.cast(this.context.getValue(key));
    }

    @Override
    public void setMessageObject(String key, MessageObject value) {
        this.context.setValue(key, value);
    }

    private String checkValueIsNull(Object o) {
        if (o != null) {
            return o.toString();
        }
        return "";
    }

    private String filterCharacter(String xpath, String character) {
        String[] strs = xpath.split("/");
        StringBuilder finalXpath = new StringBuilder("/");
        for (int i = 1; i < strs.length; i++) {
            String str = strs[i];
            StringBuilder tempXpath = new StringBuilder();
            String[] p_name = str.split(character);
            if (p_name.length > 1) {
                tempXpath.append(p_name[0]);
                for (int j = 1; j < p_name.length; j++) {
                    tempXpath.append(toUpperCase4FirstCharacter(p_name[j]));
                    strs[i] = tempXpath.toString();
                }
            }
        }
        for (int i = 1; i < strs.length; i++) {
            if (i == strs.length - 1) {
                finalXpath.append(strs[i]);
                break;
            }
            finalXpath.append(strs[i] + "/");
        }
        return finalXpath.toString();
    }

    private String toUpperCase4FirstCharacter(String str) {
        return str.replaceFirst(Character.toString(str.charAt(0)),
                Character.toString(Character.toUpperCase(str.charAt(0))));
    }

    private void setObjectValue(String xpath, Object val) {
        if (xpath.contains("_")) {
            this.context.createPathAndSetValue(filterCharacter(xpath, "_"), val);
        } else if (xpath.contains("-")) {
            this.context.createPathAndSetValue(filterCharacter(xpath, "_"), val);
        } else {
            this.context.createPathAndSetValue(xpath, val);
        }
    }

    private Object getObjectValue(String xpath) {

        if (xpath.contains("_")) {
            return context.getValue(filterCharacter(xpath, "_"));
        } else if (xpath.contains("-")) {
            return context.getValue(filterCharacter(xpath, "_"));
        } else {
            return context.getValue(xpath);
        }
    }
}
