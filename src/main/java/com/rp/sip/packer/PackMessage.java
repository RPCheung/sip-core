package com.rp.sip.packer;

import com.rp.sip.component.MessageObject;
import com.rp.sip.component.MessageType;
import com.rp.sip.model.MessageModel;
import io.netty.buffer.ByteBuf;

/**
 * Created by cheungrp on 17/11/28.
 */
public interface PackMessage {

    MessageObject unpackMessage(ByteBuf request, MessageType messageType,String txCode);

    ByteBuf packMessage(MessageObject response, MessageType messageType,String txCode);


}
