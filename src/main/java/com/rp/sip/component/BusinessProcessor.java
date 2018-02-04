package com.rp.sip.component;


import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.Map;

/**
 * Created by cheungrp on 17/10/28.
 */
public interface BusinessProcessor {

    void setBusinessProcessorId(String id);

    MessageObject executeWorkFlow() throws Exception;

    void setProcessorContext(Map<String, Object> context);

    void setInMessage(MessageObject messageModel);

    void setOutMessage(MessageObject messageModel);

    void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory);

    void setSqlSession(SqlSession sqlSession);
}
