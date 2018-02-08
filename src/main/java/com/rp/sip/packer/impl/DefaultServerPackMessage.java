package com.rp.sip.packer.impl;

import com.rp.sip.component.MessageObject;
import com.rp.sip.component.MessageType;
import com.rp.sip.db.mapper.SipSettingDAO;
import com.rp.sip.db.mapper.SipTranDAO;
import com.rp.sip.message.DefaultMessageObject;
import com.rp.sip.packer.PackMessage;
import com.rp.sip.utils.MsgUtils;
import com.rp.sip.utils.SpringBeanUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.Map;

/**
 * Created by cheungrp on 18/1/10.
 */
@Component("defaultMessagePacker")
public class DefaultServerPackMessage implements PackMessage {

    private Logger loggerMsg = LogManager.getLogger("com.rp.sip.SipMsg");
    private Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public MessageObject unpackMessage(ByteBuf request, MessageType messageType, String txCode) {
        byte[] message = new byte[request.readableBytes()];
        MessageObject messageObject;
        request.readBytes(message);
        String host = (String) getSettings().get("host");
        String msgClassName = (String) getTran(host, txCode).get("req_msg_class");
        Object o = MsgUtils.UTILS.unpackMessage(message, msgClassName);
        JXPathContext context = JXPathContext.newContext(o);
        messageObject = new DefaultMessageObject(context);
        return messageObject;
    }

    private Map<String, Object> getSettings() {
        SipSettingDAO settingDAO = SpringBeanUtils.UTILS.getSpringBeanByType(SipSettingDAO.class);
        return settingDAO.querySetting();
    }

    private Map<String, Object> getTran(String host, String txCode) {
        SipTranDAO sipTranDAO = SpringBeanUtils.UTILS.getSpringBeanByType(SipTranDAO.class);
        return sipTranDAO.queryTranByTxCode(host, txCode);
    }

    @Override
    public ByteBuf packMessage(MessageObject response, MessageType messageType, String txCode) {
        DefaultMessageObject messageObject = (DefaultMessageObject) response;
        return Unpooled.copiedBuffer(MsgUtils.UTILS.packMessage(messageObject.getSipMessagePojo()));
    }
}
