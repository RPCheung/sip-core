package com.rp.sip.component;


import org.apache.ibatis.session.SqlSession;

/**
 * Created by cheungrp on 17/10/28.
 */
public interface BusinessProcessor {

    MessageObject executeWorkFlow() throws Exception;

    void setUserComponent(UserComponent component);

    void setSqlSession(SqlSession sqlSession);


}
