package com.rp.sip.route;

import com.rp.sip.component.IMessageInterceptor;
import com.rp.sip.component.MessageObject;
import com.rp.sip.route.handlers.PoolChannelHandler;
import com.rp.sip.route.packer.PackMessage;
import com.rp.sip.route.pool.SipFixedChannelPool;
import com.rp.sip.utils.CommonUtils;
import com.rp.sip.utils.MsgUtils;
import com.rp.sip.utils.SpringBeanUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.apache.http.Header;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * Created by cheungrp on 18/2/9.
 */
public class TcpRoute implements IRoute {

    private boolean isShortConnection = false;
    private InetSocketAddress address;
    private PoolChannelHandler handler;
    private PackMessage packMessage;
    private IMessageInterceptor messageInterceptor;
    private HostCallBack.RouteReceiveMessageHandler receiveMessageHandler;
    private SipFixedChannelPool channelPool = (SipFixedChannelPool) SpringBeanUtils.UTILS.getSpringBeanById("routeChannelPool");
    private String associationId;
    private long timeout;
    private Charset charset;

    private Logger loggerMsg = LogManager.getLogger("com.rp.sip.SipMsg");
    private Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public void setAssociationId(String associationId) {
        this.associationId = associationId;
    }

    @Override
    public void addHttpRequestHeader(String key, String value) {

    }

    @Override
    public Header[] getResponseAllHeaders() {
        return new Header[0];
    }


    @Override
    public MessageObject sendAndReceiveMsg4User(MessageObject messageObject) throws InterruptedException {

        if ((!isShortConnection) && (associationId == null || "".equals(associationId))) {
            throw new IllegalArgumentException("associationId is not null or \"\" when not short connection");
        }

        Channel channel = channelPool.getChannelFromPool(address, handler);
        ByteBuf msg;

        try {
            if (this.messageInterceptor != null) {
                msg = this.packMessage.packMessage(this.messageInterceptor.beforeMarshal(messageObject));
                msg = MsgUtils.UTILS.bytes2ByteBuf(this.messageInterceptor.afterMarshal(MsgUtils.UTILS.byteBuf2Bytes(msg)));
                // 发出去的数据 自动释放
                msg = sendAndReceiveMsg4Route(msg, channel);
                msg = MsgUtils.UTILS.bytes2ByteBuf(this.messageInterceptor.afterMarshal(MsgUtils.UTILS.byteBuf2Bytes(msg)));
                return this.packMessage.unpackMessage(msg);
            } else {
                msg = this.packMessage.packMessage(messageObject);
                msg = sendAndReceiveMsg4Route(msg, channel);
                return this.packMessage.unpackMessage(msg);
            }
        } catch (Exception e) {
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
            CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, e);
            return null;
        }
    }

    public TcpRoute(boolean isShortConnection,
                    InetSocketAddress address,
                    PoolChannelHandler handler,
                    PackMessage packMessage,
                    IMessageInterceptor messageInterceptor,
                    HostCallBack.RouteReceiveMessageHandler receiveMessageHandler,
                    long timeout,
                    Charset charset) {
        this.isShortConnection = isShortConnection;
        this.handler = handler;
        this.packMessage = packMessage;
        this.messageInterceptor = messageInterceptor;
        this.receiveMessageHandler = receiveMessageHandler;
        this.address = address;
        this.timeout = timeout;
        this.charset = charset;

    }

    private ByteBuf sendAndReceiveMsg4Route(ByteBuf msg, Channel channel) throws InterruptedException {

        logger.info("路由发送请求: " + msg.toString(charset));
        loggerMsg.info("路由发送请求: " + msg.toString(charset));
        channel.write(msg);
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER);

        HostCallBack callBack;
        if (this.isShortConnection) {
            callBack = new HostCallBack(channel, this.timeout, this.receiveMessageHandler);
        } else {
            callBack = new HostCallBack(this.associationId, this.timeout, this.receiveMessageHandler);
        }

        ByteBuf respMsg = callBack.getResponseByteBuf();
        if (this.isShortConnection) {
            channelPool.releaseChannel(this.address, channel);
        }
        if (respMsg == null) {
            logger.error("路由接收响应失败!!!");
            loggerMsg.error("路由接收响应失败!!!");
            return null;
        }
        logger.info("路由收到响应:" + respMsg.toString(charset));
        loggerMsg.info("路由收到响应:" + respMsg.toString(charset));
        return respMsg;
    }
}
