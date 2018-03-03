package com.rp.sip.route.packer;

import com.rp.sip.component.MessageObject;
import com.rp.sip.component.MessageType;
import io.netty.buffer.ByteBuf;

/**
 * Created by cheungrp on 18/2/7.
 */
public interface PackMessage {

    MessageObject unpackMessage(ByteBuf response) throws Exception;

    ByteBuf packMessage(MessageObject request) throws Exception;
}
