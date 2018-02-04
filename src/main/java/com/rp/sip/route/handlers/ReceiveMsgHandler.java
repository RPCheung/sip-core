package com.rp.sip.route.handlers;

import com.rp.sip.route.HostCallBack;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;

/**
 * Created by cheungrp on 17/8/3.
 */
public class ReceiveMsgHandler extends ChannelInboundHandlerAdapter {

//    private Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());
//
//    private Logger loggerErr = LogManager.getLogger("heartbeatErr");

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        HostCallBack.receive(ctx.channel().id().asLongText(), (ByteBuf) msg);

        //ctx.channel().attr(AttributeKey.valueOf("msg")).set(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
        ctx.channel().disconnect();
        ctx.channel().deregister();
        ctx.pipeline().remove(this);
     //   logger.error(CommonUtils.getCommonUtils().printExceptionFormat(cause));
     //   loggerErr.error(CommonUtils.getCommonUtils().printExceptionFormat(cause));
    }
}
