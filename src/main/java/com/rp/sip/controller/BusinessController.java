package com.rp.sip.controller;


import com.rp.sip.component.MessageObject;
import com.rp.sip.component.BusinessProcessor;
import com.rp.sip.utils.ClassLoadUtils;
import com.rp.sip.utils.CommonUtils;
import com.rp.sip.utils.DBUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by RP on 2017/5/3.
 */
public class BusinessController extends ChannelInboundHandlerAdapter {

    private Logger loggerMsg = LogManager.getLogger("com.rp.sip.SipMsg");
    private Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg == null) {
            throw new RuntimeException("create BusinessModel error");
        }

        Thread.currentThread().setContextClassLoader(ClassLoadUtils.utils.getSipUserClassloader());

        Map<String, BusinessProcessor> processorEntry = (Map<String, BusinessProcessor>) msg;
        String txCode = processorEntry.keySet().iterator().next();
        BusinessProcessor businessProcessor = processorEntry.remove(txCode);

        SqlSession session = DBUtils.UTILS.getUserSqlSessionFactory().openSession(true);
        businessProcessor.setSqlSession(session);
        MessageObject resp = businessProcessor.executeWorkFlow();
        session.close();


        Map<String, MessageObject> finalTxEntry = new ConcurrentHashMap<>(1);
        if (resp == null) {
            throw new NullPointerException("response msg can't be null .");
        }
        finalTxEntry.put(txCode, resp);
        ctx.channel().writeAndFlush(finalTxEntry);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        CommonUtils.getCommonUtils().printExceptionFormat(logger, cause);
        CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, cause);
    }


}
