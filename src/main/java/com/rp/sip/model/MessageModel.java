package com.rp.sip.model;

import com.rp.sip.component.MessageObject;
import com.rp.sip.component.MessageType;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by cheungrp on 17/11/27.
 */
public class MessageModel implements Serializable {

    private MessageObject request;
    private MessageObject response;
    private MessageType messageType;
    private Map<String, String> context;

    public MessageModel(MessageType messageType) {
        this.messageType = messageType;
        this.context = new ConcurrentHashMap<>(64);
    }

    public void setRequest(MessageObject request) {
        this.request = request;
    }

    public void setResponse(MessageObject response) {
        this.response = response;
    }

    public MessageObject getRequest() {
        return request;
    }

    public MessageObject getResponse() {
        return response;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public Object addAttr(String key, String value) {
        return this.context.put(key, value);
    }

    public String removeAttr(String key) {
        return this.context.remove(key);
    }

    public void cleanAttr() {
        this.context.clear();
    }


}
