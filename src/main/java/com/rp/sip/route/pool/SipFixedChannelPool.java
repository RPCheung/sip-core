package com.rp.sip.route.pool;

import com.rp.sip.db.mapper.*;
import com.rp.sip.route.handlers.PoolChannelHandler;
import com.rp.sip.utils.CommonUtils;
import com.rp.sip.utils.SpringBeanUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by cheungrp on 18/2/9.
 */
@Component("routeChannelPool")
public class SipFixedChannelPool {


    private static ChannelPoolMap<InetSocketAddress, FixedChannelPool> poolMap;
    private static EventLoopGroup group = null;
    private static Bootstrap bootstrap = new Bootstrap();
    private static PoolChannelHandler channelHandler;

    private Logger loggerMsg = LogManager.getLogger("com.rp.sip.SipMsg");
    private Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    public void init() {
        if (!(Boolean.valueOf((String) getSettings().get("isUserRoute")))) {
            return;
        }
        group = new NioEventLoopGroup(Integer.parseInt((String) getRoutePoolSetting().get("IONum")));
        bootstrap.group(group)
                .channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                .option(ChannelOption.SO_KEEPALIVE, true);
        SipFixedChannelPool.poolMap = new AbstractChannelPoolMap<InetSocketAddress, FixedChannelPool>() {
            @Override
            protected FixedChannelPool newPool(InetSocketAddress key) {
                ChannelPoolHandler handler = new ChannelPoolHandler() {
                    @Override
                    public void channelReleased(Channel ch) throws Exception {
                        logger.info("与 [" + ch.remoteAddress() + "] 释放了连接.");
                        loggerMsg.info("与 [" + ch.remoteAddress() + "] 释放了连接.");
                    }

                    @Override
                    public void channelCreated(Channel ch) throws Exception {
                        SipFixedChannelPool.channelHandler.addHandlers(ch.pipeline());
                    }

                    @Override
                    public void channelAcquired(Channel ch) throws Exception {

                    }
                };
                return new FixedChannelPool(bootstrap.remoteAddress(key), handler, Integer.parseInt((String) getRoutePoolSetting().get("maxConnections")));
            }
        };
    }

    public Channel getChannelFromPool(InetSocketAddress address, PoolChannelHandler poolChannelHandler) {
        Future<Channel> future;
        Channel channel = null;
        try {
            SipFixedChannelPool.channelHandler = poolChannelHandler;
            future = SipFixedChannelPool.poolMap.get(address).acquire();
            channel = future.get();
            logger.info("[" + channel.remoteAddress() + "] 获取了连接.");
            loggerMsg.info("[" + channel.remoteAddress() + "] 获取了连接.");
        } catch (InterruptedException | ExecutionException e) {
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
        }
        return channel;
    }

    public void releaseChannel(InetSocketAddress address, Channel channel) {
        SipFixedChannelPool.poolMap.get(address).release(channel);
    }

    private Map<String, Object> getSettings() {
        SipSettingDAO settingDAO = SpringBeanUtils.UTILS.getSpringBeanByType(SipSettingDAO.class);
        return settingDAO.querySetting();
    }

    private Map<String, Object> getRoutePoolSetting() {
        RoutePoolSettingDAO routePoolSetting = SpringBeanUtils.UTILS.getSpringBeanByType(RoutePoolSettingDAO.class);
        return routePoolSetting.queryRoutePoolSetting();
    }

}
