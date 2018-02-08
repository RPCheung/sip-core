package com.rp.sip.route.packer.impl;

import com.rp.sip.component.MessageObject;
import com.rp.sip.message.DefaultMessageObject;
import com.rp.sip.route.packer.PackMessage;
import com.rp.sip.utils.MsgUtils;
import io.netty.buffer.ByteBuf;
import org.apache.commons.jxpath.JXPathContext;
import org.springframework.stereotype.Component;


/**
 * Created by cheungrp on 18/2/7.
 */
@Component("routePackMessage")
public class PackMessageImpl implements PackMessage {

    private String resClassName;

    @Override
    public MessageObject unpackMessage(ByteBuf response) {
        return new DefaultMessageObject(JXPathContext.newContext(MsgUtils.UTILS.unpackMessage(MsgUtils.UTILS.byteBuf2Bytes(response), resClassName)));
    }

    @Override
    public ByteBuf packMessage(MessageObject request) {
        return MsgUtils.UTILS.bytes2ByteBuf(MsgUtils.UTILS.packMessage(request.getSipMessagePojo()));
    }

    public void setResClassName(String resClassName) {
        this.resClassName = resClassName;
    }
}
