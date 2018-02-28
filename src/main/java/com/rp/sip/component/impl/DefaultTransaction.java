package com.rp.sip.component.impl;

import com.rp.sip.component.ITransaction;
import com.rp.sip.component.MessageObject;
import com.rp.sip.route.IRoute;
import org.apache.ibatis.session.SqlSessionFactory;


import java.util.Map;

/**
 * Created by cheungrp on 18/1/11.
 */
public class DefaultTransaction implements ITransaction {

    private String txCode;
    private Map<String, Object> context;
    private MessageObject requestMessage;
    private MessageObject responseMessage;

    private MessageObject routeRequestMessage;
    private MessageObject routeResponseMessage;
    private IRoute route;

    private SqlSessionFactory sessionFactory;


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
    public void setRouteRequestMessage(MessageObject messageObject) {
        this.routeRequestMessage = messageObject;
    }

    @Override
    public MessageObject getRouteRequestMessage() {
        return this.routeRequestMessage;
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
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sessionFactory = sqlSessionFactory;
    }

    @Override
    public SqlSessionFactory getSqlSessionFactory() {
        return this.sessionFactory;
    }

    @Override
    public void setRoute(IRoute route) {
        this.route = route;
    }

    @Override
    public IRoute getRoute() {
        return this.route;
    }

}
