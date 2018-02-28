package com.rp.sip.component;


import com.rp.sip.route.IRoute;
import org.apache.ibatis.session.SqlSessionFactory;

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

    void setRouteRequestMessage(MessageObject messageObject);

    MessageObject getRouteRequestMessage();

    void setResponseMessage(MessageObject messageObject);

    MessageObject getResponseMessage();

    void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory);

    SqlSessionFactory getSqlSessionFactory();

    void setRoute(IRoute route);

    IRoute getRoute();


}
