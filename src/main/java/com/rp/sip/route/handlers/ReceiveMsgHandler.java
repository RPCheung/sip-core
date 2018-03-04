package com.rp.sip.route.handlers;

import com.rp.sip.component.IMessageInterceptor;
import com.rp.sip.component.MessageObject;
import com.rp.sip.route.HostCallBack;
import com.rp.sip.route.packer.PackMessage;
import com.rp.sip.utils.CommonUtils;
import com.rp.sip.utils.MsgUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;

/**
 * Created by cheungrp on 17/8/3.
 */
public class ReceiveMsgHandler extends ChannelInboundHandlerAdapter {

    private Logger loggerMsg = LogManager.getLogger("com.rp.sip.SipMsg");
    private Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private PackMessage packMessage;
    private IMessageInterceptor messageInterceptor;
    private HostCallBack.RouteReceiveMessageHandler handler;
    private boolean isShortConnection;
    private Charset charset;

    public ReceiveMsgHandler(PackMessage packMessage,
                             IMessageInterceptor messageInterceptor,
                             HostCallBack.RouteReceiveMessageHandler handler,
                             boolean isShortConnection,
                             Charset charset) {
        this.packMessage = packMessage;
        this.messageInterceptor = messageInterceptor;
        this.handler = handler;
        this.isShortConnection = isShortConnection;
        this.charset = charset;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            ByteBuf msgBuf = (ByteBuf) msg;
            logger.info("路由收到响应:" + msgBuf.toString(charset));
            loggerMsg.info("路由收到响应:" + msgBuf.toString(charset));
            if (handler != null) {
                // unpackMessagePreprocess 里释放 ByteBuf (msgBuf)
                msgBuf = this.handler.unpackMessagePreprocess(msgBuf);
            }
            if (this.messageInterceptor != null) {
                msgBuf = MsgUtils.UTILS.bytes2ByteBuf(
                        this.messageInterceptor.beforeUnmarshal(MsgUtils.UTILS.byteBuf2Bytes(msgBuf)));
                MessageObject messageObject = this.packMessage.unpackMessage(msgBuf);
                messageObject = this.messageInterceptor.afterUnmarshal(messageObject);
                if (isShortConnection) {
                    HostCallBack.receive(ctx.channel().id().asLongText(), messageObject);
                } else {
                    HostCallBack.receive(messageObject.
                            getString((String)
                                    ctx.channel().attr(
                                            AttributeKey.valueOf("associationIdXPath")).get()), messageObject);
                }

            } else {
                MessageObject messageObject = this.packMessage.unpackMessage(msgBuf);
                if (isShortConnection) {
                    HostCallBack.receive(ctx.channel().id().asLongText(), messageObject);
                } else {
                    HostCallBack.receive(messageObject.
                            getString((String)
                                    ctx.channel().attr(
                                            AttributeKey.valueOf("associationIdXPath")).get()), messageObject);
                }
            }
        } catch (Exception e) {
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
            CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
        ctx.channel().disconnect();
        ctx.channel().deregister();
        ctx.pipeline().remove(this);
        CommonUtils.getCommonUtils().printExceptionFormat(logger, cause);
        CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, cause);
    }
}
