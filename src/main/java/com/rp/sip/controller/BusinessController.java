package com.rp.sip.controller;


import com.rp.sip.component.MessageObject;
import com.rp.sip.component.BusinessProcessor;
import com.rp.sip.utils.ClassLoaderUtils;
import com.rp.sip.utils.CommonUtils;
import com.rp.sip.utils.DBUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
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
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        SqlSession session = null;
        try {
            if (msg == null) {
                throw new RuntimeException("create BusinessModel error");
            }

            Thread.currentThread().setContextClassLoader(ClassLoaderUtils.utils.getSipUserClassloader());

            Map<String, BusinessProcessor> processorEntry = (Map<String, BusinessProcessor>) msg;
            String txCode = processorEntry.keySet().iterator().next();
            BusinessProcessor businessProcessor = processorEntry.remove(txCode);

            session = DBUtils.UTILS.getUserSqlSessionFactory().openSession(true);
            businessProcessor.setSqlSession(session);
            MessageObject resp = businessProcessor.executeWorkFlow();

            Map<String, MessageObject> finalTxEntry = new ConcurrentHashMap<>(1);
            if (resp == null) {
                throw new NullPointerException("response msg can't be null .");
            }
            finalTxEntry.put(txCode, resp);
            ChannelFuture channelFuture = ctx.channel().writeAndFlush(finalTxEntry);
            channelFuture.addListener((ChannelFutureListener) future -> {
                boolean isDone = future.isDone();
                boolean isSuccess = future.isSuccess();
                logger.info("交易是否完成: " + isDone);
                loggerMsg.info("交易是否完成: " + isDone);
                logger.info("交易是否成功: " + isSuccess);
                loggerMsg.info("交易是否成功: " + isSuccess);
            });
        } catch (Exception e) {
            if (session != null) {
                session.rollback();
                logger.error("出现异常!!!  事务被回滚.");
                loggerMsg.error("出现异常!!!  事务被回滚.");
            }
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
            CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                ctx.writeAndFlush(Unpooled.copiedBuffer(new byte[]{0, 0}));
            } else if (e.state() == IdleState.WRITER_IDLE) {
                ctx.writeAndFlush(Unpooled.copiedBuffer(new byte[]{0, 0}));
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        CommonUtils.getCommonUtils().printExceptionFormat(logger, cause);
        CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, cause);
    }


}
