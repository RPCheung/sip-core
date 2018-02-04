package com.rp.sip.component;


import java.util.Map;

/**
 * Created by cheungrp on 17/12/1.
 */
public interface ITransaction {

    String getTxCode();

    void setTransactionContext(Map<String, Object> context);

    Map<String, Object> getTransactionContext();

    void setRequestMessage(MessageObject messageObject);

    MessageObject getRequestMessage();

    void setResponseMessage(MessageObject messageObject);

    MessageObject getResponseMessage();

    BusinessProcessor getBusiness();

    void setBusiness(BusinessProcessor businessProcessor);

    Object getSipDbSession();

    void setSipDbSession();

}
