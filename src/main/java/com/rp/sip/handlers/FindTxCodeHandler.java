package com.rp.sip.handlers;

import com.rp.sip.component.MessageType;
import io.netty.buffer.ByteBuf;

import java.util.Map;

/**
 * Created by cheungrp on 18/1/10.
 */
public interface FindTxCodeHandler {

    String findTxCodeFromReqMsg(ByteBuf request,MessageType messageType,Map<String, Object> setting);

    void setTxCodePath(String txCodePath);

}
