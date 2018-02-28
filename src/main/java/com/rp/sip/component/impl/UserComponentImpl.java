package com.rp.sip.component.impl;

import com.rp.sip.component.ITransaction;
import com.rp.sip.component.MessageObject;
import com.rp.sip.component.UserComponent;
import com.rp.sip.route.IRoute;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by cheungrp on 18/2/7.
 */
public class UserComponentImpl implements UserComponent {

    private ITransaction transaction;

    private Map<String, Object> context = new ConcurrentHashMap<>(128);

    public UserComponentImpl(ITransaction transaction) {
        this.transaction = transaction;
    }


    @Override
    public Map<String, Object> getProcessorContext() {
        return this.context;
    }

    @Override
    public MessageObject getInMessage() {
        return transaction.getRequestMessage();
    }

    @Override
    public MessageObject getOutMessage() {
        return transaction.getResponseMessage();
    }

    @Override
    public SqlSessionFactory getSqlSessionFactory() {
        return transaction.getSqlSessionFactory();
    }

    @Override
    public MessageObject getRouteRequestMessage() {
        return transaction.getRouteRequestMessage();
    }

    @Override
    public IRoute getRoute() {
        return transaction.getRoute();
    }


}
