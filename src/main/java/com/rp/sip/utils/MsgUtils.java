package com.rp.sip.utils;


import com.alibaba.druid.filter.config.ConfigTools;
import com.alibaba.fastjson.JSON;
import com.rp.sip.classloader.SipUserClassloader;
import com.rp.sip.component.MessageObject;
import com.rp.sip.message.DefaultMessageObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.jxpath.JXPathContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.lang.invoke.MethodHandles;


/**
 * Created by RP on 2017/5/16.
 */
public enum MsgUtils {

    UTILS;
    private Logger loggerMsg = LogManager.getLogger("com.rp.sip.SipMsg");
    private Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    @Deprecated
    public MessageObject createMessageObject(String messageBeanPackageName, String txCode) {
        try {
            Object o = Class.forName(messageBeanPackageName + "." + "T" + txCode).newInstance();
            JXPathContext context = JXPathContext.newContext(o);
            return new DefaultMessageObject(context);
        } catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
            CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, e);
            return null;
        }
    }

    public <T> T unpackMessage(byte[] message, String clzz) {
        if (message.length == 0) {
            return null;
        }
        SipUserClassloader classloader = SpringBeanUtils.UTILS.getSpringBeanByType(SipUserClassloader.class);
        try {
            return JSON.parseObject(message, classloader.loadClass(clzz));
        } catch (ClassNotFoundException e) {
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
            return null;
        }
    }

    public byte[] packMessage(Object message) {
        return JSON.toJSONBytes(message);
    }


    public String findTxCode4Xml(String txCodePath, String xml) throws DocumentException {
        SAXReader saxReader = new SAXReader();
        StringReader in = new StringReader(xml);
        Document document = saxReader.read(in);
        return document.getRootElement().selectSingleNode(txCodePath).getText().trim();
    }


    public MessageObject createMessageObject(Object message) {
        return new DefaultMessageObject(JXPathContext.newContext(message));
    }

    public byte[] byteBuf2Bytes(ByteBuf message) {
        byte[] messageBytes = new byte[message.readableBytes()];
        message.readBytes(messageBytes);
        return messageBytes;
    }

    public ByteBuf bytes2ByteBuf(byte[] message) {
        return Unpooled.copiedBuffer(message);
    }


}
