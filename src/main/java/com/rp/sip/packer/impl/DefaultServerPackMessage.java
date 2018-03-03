package com.rp.sip.packer.impl;

import com.rp.sip.component.MessageObject;
import com.rp.sip.component.MessageType;
import com.rp.sip.db.mapper.SipSettingDAO;
import com.rp.sip.db.mapper.SipTranDAO;
import com.rp.sip.message.DefaultMessageObject;
import com.rp.sip.model.SIPInfo;
import com.rp.sip.packer.PackMessage;
import com.rp.sip.utils.ClassLoaderUtils;
import com.rp.sip.utils.MsgUtils;
import com.rp.sip.utils.SpringBeanUtils;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.ClassLoaderReference;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.io.IOUtils;
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
    public MessageObject unpackMessage(ByteBuf request, String txCode) throws Exception {
        byte[] message = MsgUtils.UTILS.byteBuf2Bytes(request);
        MessageObject messageObject = null;
        String host = (String) getSettings().get("host");
        String charset = (String) getSettings().get("charset");
        MessageType messageType = MessageType.valueOf((String) getSettings().get("msgType"));
        String msgClassName = (String) getTran(host, txCode).get("req_msg_class");
        String rootElementName = (String) getTran(host, txCode).get("xmlRootName");

        switch (messageType) {
            case XML: {
                String xml = IOUtils.toString(message, charset);

                XStream stream = new XStream(null,
                        new DomDriver(charset, new XmlFriendlyNameCoder("__", "_")),
                        new ClassLoaderReference(ClassLoaderUtils.utils.getSipUserClassloader()));
                stream.setMode(XStream.NO_REFERENCES);
                stream.alias(rootElementName, ClassLoaderUtils.utils.createSipUserClass(msgClassName));
                messageObject = new DefaultMessageObject(JXPathContext.newContext(stream.fromXML(xml)));
                break;
            }
            case OBJ: {
                Object o = MsgUtils.UTILS.unpackMessage(message, msgClassName);
                JXPathContext context = JXPathContext.newContext(o);
                messageObject = new DefaultMessageObject(context);
                break;
            }
            case JSON: {
            }
            case AUTO: {
            }
        }
        return messageObject;
    }

    private Map<String, Object> getSettings() {
        SipSettingDAO settingDAO = SpringBeanUtils.UTILS.getSpringBeanByType(SipSettingDAO.class);
        SIPInfo info = (SIPInfo) SpringBeanUtils.UTILS.getSpringBeanById("sip-info");
        return settingDAO.querySetting(info.getServerId());
    }

    private Map<String, Object> getTran(String host, String txCode) {
        SipTranDAO sipTranDAO = SpringBeanUtils.UTILS.getSpringBeanByType(SipTranDAO.class);
        return sipTranDAO.queryTranByTxCode(host, txCode);
    }

    @Override
    public ByteBuf packMessage(MessageObject response, String txCode) throws Exception {
        DefaultMessageObject messageObject = (DefaultMessageObject) response;
        String charset = (String) getSettings().get("charset");
        String host = (String) getSettings().get("host");
        MessageType messageType = MessageType.valueOf((String) getSettings().get("msgType"));
        String rootElementName = (String) getTran(host, txCode).get("xmlRootName");
        String msgClassName = (String) getTran(host, txCode).get("res_msg_class");

        switch (messageType) {

            case XML: {
                XStream stream = new XStream(null,
                        new DomDriver(charset, new XmlFriendlyNameCoder("__", "_")),
                        new ClassLoaderReference(ClassLoaderUtils.utils.getSipUserClassloader()));
                stream.setMode(XStream.NO_REFERENCES);
                stream.alias(rootElementName, ClassLoaderUtils.utils.createSipUserClass(msgClassName));
                String xml = stream.toXML(messageObject.getSipMessagePojo());
                return Unpooled.copiedBuffer(xml.getBytes());
            }
            case OBJ: {
                return Unpooled.copiedBuffer(MsgUtils.UTILS.packMessage(messageObject.getSipMessagePojo()));
            }
            case JSON: {
                return null;
            }
            case AUTO: {
                return null;
            }
            default: {
                return null;
            }
        }
    }
}
