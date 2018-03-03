package com.rp.sip.packer;

import com.rp.sip.component.MessageObject;
import io.netty.buffer.ByteBuf;

/**
 * Created by cheungrp on 17/11/28.
 */
public interface PackMessage {

    MessageObject unpackMessage(ByteBuf request, String txCode) throws Exception;

    ByteBuf packMessage(MessageObject response, String txCode) throws Exception;


}
