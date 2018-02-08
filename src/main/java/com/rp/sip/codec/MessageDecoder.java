package com.rp.sip.codec;

import com.rp.sip.component.IMessageInterceptor;
import com.rp.sip.component.ITransaction;
import com.rp.sip.component.MessageObject;
import com.rp.sip.handlers.FindTxCodeHandler;
import com.rp.sip.message.DefaultMessageObject;
import com.rp.sip.component.MessageType;
import com.rp.sip.model.MessageModel;
import com.rp.sip.packer.PackMessage;
import com.rp.sip.utils.ModelUtils;
import com.rp.sip.utils.SpringBeanUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.nio.Buffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by cheungrp on 17/10/28.
 */
public class MessageDecoder extends MessageToMessageDecoder<Map<String, ByteBuf>> {

    private Logger loggerMsg = LogManager.getLogger("com.rp.sip.SipMsg");
    private Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private String charset;
    private MessageType messageType;

    public MessageDecoder(String charset, MessageType messageType) {
        this.charset = charset;
        this.messageType = messageType;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, Map<String, ByteBuf> txEntry, List<Object> out) throws Exception {

        String txCode = txEntry.keySet().iterator().next();
        ByteBuf msg = txEntry.remove(txCode);

        logger.info("server receive msg:" + msg.toString(Charset.forName(charset)));
        loggerMsg.info("server receive msg:" + msg.toString(Charset.forName(charset)));
        PackMessage packMessage;
        IMessageInterceptor messageInterceptor = (IMessageInterceptor) SpringBeanUtils.UTILS.getSpringBeanById("messageInterceptor");

        if (SpringBeanUtils.UTILS.isContainsBean4Id("customMessagePacker")) {
            packMessage = (PackMessage) SpringBeanUtils.UTILS.getSpringBeanById("customMessagePacker");
        } else {
            packMessage = (PackMessage) SpringBeanUtils.UTILS.getSpringBeanById("defaultMessagePacker");
        }

        // 从数据库 获取 msgType
        MessageType messageType = this.messageType;
        MessageObject messageObject;
        // 打解包  是为了 让 ByteBuf 转换为 MessageObject
        if (messageInterceptor != null) {
            byte[] messageBytes = new byte[msg.readableBytes()];
            msg.copy().readBytes(messageBytes);
            messageBytes = messageInterceptor.beforeUnmarshal(messageBytes);
            messageObject = packMessage.unpackMessage(Unpooled.copiedBuffer(messageBytes), messageType, txCode);
            messageInterceptor.afterUnmarshal(messageObject);
        } else {
            messageObject = packMessage.unpackMessage(msg, messageType, txCode);
        }
        if (messageObject == null) {
            throw new NullPointerException("messageInterceptor or packMessage return null");
        }

        // 彻底释放 ByteBuf
        int refCnt = msg.refCnt();
        if (refCnt > 0) {
            msg.release(refCnt);
        }

        Map<String, MessageObject> finalTxEntry = new ConcurrentHashMap<>(1);
        finalTxEntry.put(txCode, messageObject);
        out.add(finalTxEntry);
    }


}
