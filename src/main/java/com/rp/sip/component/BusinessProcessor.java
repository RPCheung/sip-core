package com.rp.sip.component;


import com.rp.sip.route.SipRouteClient;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.Map;

/**
 * Created by cheungrp on 17/10/28.
 */
public interface BusinessProcessor {

    MessageObject executeWorkFlow() throws Exception;

    void setUserComponent(UserComponent component);

    void setSqlSession(SqlSession sqlSession);


}
