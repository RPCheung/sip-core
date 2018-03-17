package com.rp.sip.codec;

import com.rp.sip.component.IMessageInterceptor;
import com.rp.sip.component.MessageObject;
import com.rp.sip.component.MessageType;
import com.rp.sip.db.mapper.SipTranDAO;
import com.rp.sip.model.SIPInfo;
import com.rp.sip.packer.PackMessage;
import com.rp.sip.utils.ClassLoaderUtils;
import com.rp.sip.utils.SpringBeanUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Created by cheungrp on 17/10/28.
 */
public class MessageEncoder extends MessageToByteEncoder<Map<String, MessageObject>> {

    private Logger loggerMsg = LogManager.getLogger("com.rp.sip.SipMsg");
    private Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private String charset;

    public MessageEncoder(String charset, MessageType messageType) {
        this.charset = charset;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Map<String, MessageObject> msg, ByteBuf out) throws Exception {

        PackMessage packMessage;

        if (SpringBeanUtils.UTILS.isContainsBean4Id("customMessagePacker")) {
            packMessage = (PackMessage) SpringBeanUtils.UTILS.getSpringBeanById("customMessagePacker");
        } else {
            packMessage = (PackMessage) SpringBeanUtils.UTILS.getSpringBeanById("defaultMessagePacker");
        }

        Map<String, MessageObject> finalTxEntry = msg;

        String txCode = finalTxEntry.keySet().iterator().next();
        MessageObject messageObject = finalTxEntry.remove(txCode);

        IMessageInterceptor messageInterceptor = null;

        if (getTran(txCode).get("resMessageInterceptor") != null) {
            messageInterceptor = (IMessageInterceptor) ClassLoaderUtils
                    .utils.createSipUserObject((String) getTran(txCode).get("resMessageInterceptor"));
        }

        ByteBuf messageByteBuf;

        if (messageInterceptor != null) {
            messageInterceptor.beforeMarshal(messageObject);
            messageByteBuf = packMessage.packMessage(messageObject, txCode);
            byte[] messageBytes = new byte[messageByteBuf.readableBytes()];
            messageByteBuf.readBytes(messageBytes);
            out.writeBytes(messageInterceptor.afterMarshal(messageBytes));
            messageByteBuf.release();
        } else {
            messageByteBuf = packMessage.packMessage(messageObject, txCode);
            out.writeBytes(messageByteBuf);
            messageByteBuf.release();
        }

        logger.info("server response msg:" + out.toString(Charset.forName(charset)));
        loggerMsg.info("server response msg:" + out.toString(Charset.forName(charset)));

    }

    private Map<String, Object> getTran(String txCode) {
        SIPInfo info = (SIPInfo) SpringBeanUtils.UTILS.getSpringBeanById("sip-info");
        SipTranDAO sipTranDAO = SpringBeanUtils.UTILS.getSpringBeanByType(SipTranDAO.class);
        return sipTranDAO.queryTranByTxCode(info.getServerId(), txCode);
    }
}
