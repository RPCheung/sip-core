package com.rp.sip.component.impl;

import com.rp.sip.component.BusinessProcessor;
import com.rp.sip.component.ITransaction;
import com.rp.sip.component.MessageObject;
import org.msgpack.annotation.Message;


import java.util.Map;

/**
 * Created by cheungrp on 18/1/11.
 */
public class DefaultTransaction implements ITransaction {

    private String txCode;
    private Map<String, Object> context;
    private MessageObject requestMessage;
    private MessageObject responseMessage;
    private BusinessProcessor businessProcessor;

    public DefaultTransaction(String txCode) {
        this.txCode = txCode;
    }

    @Override
    public String getTxCode() {
        return this.txCode;
    }

    @Override
    public void setTransactionContext(Map<String, Object> context) {
        this.context = context;
    }

    @Override
    public Map<String, Object> getTransactionContext() {
        return this.context;
    }

    @Override
    public void setRequestMessage(MessageObject messageObject) {
        this.requestMessage = messageObject;
    }

    @Override
    public MessageObject getRequestMessage() {
        return this.requestMessage;
    }

    @Override
    public void setResponseMessage(MessageObject messageObject) {
        this.responseMessage = messageObject;
    }

    @Override
    public MessageObject getResponseMessage() {
        return this.responseMessage;
    }

    @Override
    public BusinessProcessor getBusiness() {
        return this.businessProcessor;
    }

    @Override
    public void setBusiness(BusinessProcessor businessProcessor) {
        this.businessProcessor = businessProcessor;
    }

    @Override
    public Object getSipDbSession() {
        return null;
    }

    @Override
    public void setSipDbSession( ) {

    }
}
