package com.rp.sip.handlers.impl;

import com.alibaba.fastjson.JSON;
import com.rp.sip.classloader.SipUserClassloader;
import com.rp.sip.component.IMessageInterceptor;
import com.rp.sip.component.MessageType;
import com.rp.sip.db.mapper.SipSettingDAO;
import com.rp.sip.db.mapper.SipTranDAO;
import com.rp.sip.handlers.FindTxCodeHandler;
import com.rp.sip.utils.ClassLoadUtils;
import com.rp.sip.utils.CommonUtils;
import com.rp.sip.utils.MsgUtils;
import com.rp.sip.utils.SpringBeanUtils;
import io.netty.buffer.ByteBuf;

import org.apache.commons.jxpath.JXPathContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.DocumentException;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Created by cheungrp on 18/1/26.
 */
@Component("defaultFindTxCodeHandler")
public class DefaultFindTxCodeHandler implements FindTxCodeHandler {

    private Logger loggerMsg = LogManager.getLogger("com.rp.sip.SipMsg");
    private Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private String txCodePath = null;
    private JXPathContext context = null;


    @Override
    public String findTxCodeFromReqMsg(ByteBuf request, MessageType messageType, Map<String, Object> setting) {

        IMessageInterceptor messageInterceptor = (IMessageInterceptor) SpringBeanUtils.UTILS.getSpringBeanById("messageInterceptor");

        switch (messageType) {
            case XML: {
                // 15 是头部跳过字节
                // 46 是尾部跳过字节
                //  String xml = request.toString(15, request.readableBytes() - (15+46), Charset.forName(charset));
                String charset = (String) getSettings().get("charset");
                String xml = request.toString(Charset.forName(charset));
                try {
                    return MsgUtils.UTILS.findTxCode4Xml(txCodePath, xml);
                } catch (DocumentException e) {
                    CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
                    CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, e);
                    return null;
                }
            }
            case JSON: {
                return null;
            }
            case OBJ: {
                byte[] message = new byte[request.readableBytes()];
                request.readBytes(message);

                if (messageInterceptor != null) {
                    message = messageInterceptor.beforeUnmarshal(message);
                }

                Object o = MsgUtils.UTILS.unpackMessage(message, (String) setting.get("txCodePojo"));
                context = JXPathContext.newContext(o);
                return (String) context.getValue(txCodePath);
            }
            case AUTO: {
                return null;
            }
            default: {
                return null;
            }
        }
    }

    private Map<String, Object> getSettings() {
        SipSettingDAO settingDAO = SpringBeanUtils.UTILS.getSpringBeanByType(SipSettingDAO.class);
        return settingDAO.querySetting();
    }

    @Override
    public void setTxCodePath(String txCodePath) {
        this.txCodePath = txCodePath;
    }
}
