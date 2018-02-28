package com.rp.sip.component;

import com.rp.sip.route.IRoute;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.Map;

/**
 * Created by cheungrp on 18/2/7.
 */
public interface UserComponent {

    Map<String, Object> getProcessorContext();

    MessageObject getInMessage();

    MessageObject getOutMessage();

    SqlSessionFactory getSqlSessionFactory();

    MessageObject getRouteRequestMessage();

    IRoute getRoute();

}
