package com.rp.sip.route.pool;


import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.Future;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/**
 * Created by cheungrp on 17/10/31.
 */
public class ChannelFuturePoolGroup implements Ordered, ApplicationListener<ContextRefreshedEvent> {

//    private Logger loggerErr = LogManager.getLogger("heartbeatErr");
//    private Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());
//    private Logger loggerMsg = LogManager.getLogger("heartbeatMsg");

    private static ChannelPoolMap<String, FixedChannelPool> poolMap;
    private Bootstrap bootstrap = null;
    private static EventLoopGroup group = null;

    static {
        group = new NioEventLoopGroup();
    }

//    public void initPool() {
//        bootstrap = new Bootstrap();
//        bootstrap.group(group);
//        bootstrap.channel(NioSocketChannel.class);
//        bootstrap.remoteAddress(configManager.getIp(), configManager.getPort());
//        poolMap = new AbstractChannelPoolMap<String, FixedChannelPool>() {
//            @Override
//            protected FixedChannelPool newPool(String key) {
//                ChannelPoolHandler handler = new ChannelPoolHandler() {
//                    @Override
//                    public void channelReleased(Channel ch) throws Exception {
//
//                    }
//
//                    @Override
//                    public void channelAcquired(Channel ch) throws Exception {
//
//                    }
//
//                    @Override
//                    public void channelCreated(Channel ch) throws Exception {
//                        ch.pipeline().addLast(new SimpleLengthFieldPrepender(8, false, "GBK"));
//                        ch.pipeline().addLast(new SimpleLengthFieldDecoder(8, 0, "GBK"));
//                        ch.pipeline().addLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS));
//                        ch.pipeline().addLast(new ReceiveMsgHandler());
//                    }
//                };
//                return new FixedChannelPool(bootstrap, handler, 8);
//            }
//        };
//    }
//
//    public void sendAndReceiveMsg(ByteBuf msg) throws Exception {
//        FixedChannelPool pool = poolMap.get(configManager.getIp());
//        Future<Channel> future = pool.acquire();
//        Channel channel = future.get();
//        //channel.write(msg);
//        channel.write(Unpooled.copiedBuffer(configManager.getMsgHandler().writeXmlMsg(null)));
//        channel.writeAndFlush(Unpooled.EMPTY_BUFFER);
//        HostCallBack callBack = new HostCallBack(channel, 30000);
//        ByteBuf respMsg = callBack.getResponseByteBuf();
//        pool.release(channel);
//        if (respMsg == null) {
//            logger.error("接收业务平台响应失败!!!");
//            loggerErr.error("接到业务平台响应失败!!!");
//            return;
//        }
//        logger.info("收到业务平台响应:" + respMsg.toString(Charset.forName(configManager.getCharset())));
//        loggerMsg.info("收到业务平台响应:" + respMsg.toString(Charset.forName(configManager.getCharset())));
//        respMsg.release();
//    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
       // initPool();
    }
}
