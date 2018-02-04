package com.rp.sip.codec;

import com.rp.sip.component.IMessageInterceptor;
import com.rp.sip.component.MessageObject;
import com.rp.sip.component.MessageType;
import com.rp.sip.model.MessageModel;
import com.rp.sip.packer.PackMessage;
import com.rp.sip.packer.impl.DefaultPackMessage;
import com.rp.sip.utils.ModelUtils;
import com.rp.sip.utils.SpringBeanUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

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

    private MessageType messageType;

    public MessageEncoder(String charset, MessageType messageType) {
        this.charset = charset;
        this.messageType = messageType;
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

        IMessageInterceptor messageInterceptor = (IMessageInterceptor) SpringBeanUtils.UTILS.getSpringBeanById("messageInterceptor");

        ByteBuf messageByteBuf;

        if (messageInterceptor != null) {
            messageInterceptor.beforeMarshal(messageObject);
            messageByteBuf = packMessage.packMessage(messageObject, this.messageType, txCode);
            byte[] messageBytes = new byte[messageByteBuf.readableBytes()];
            messageByteBuf.readBytes(messageBytes);
            out.writeBytes(messageInterceptor.afterMarshal(messageBytes));

        } else {
            messageByteBuf = packMessage.packMessage(messageObject, this.messageType, txCode);
            out.writeBytes(messageByteBuf);
        }

        logger.info("request msg:" + out.toString(Charset.forName(charset)));
        loggerMsg.info("request msg:" + out.toString(Charset.forName(charset)));

    }
}
