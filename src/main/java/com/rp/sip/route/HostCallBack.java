package com.rp.sip.route;

import com.rp.sip.component.MessageObject;
import com.rp.sip.packer.PackMessage;
import com.rp.sip.utils.CommonUtils;
import com.rp.sip.utils.SpringBeanUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by cheungrp on 17/10/31.
 */
public class HostCallBack {


    private static Logger loggerMsg = LogManager.getLogger("com.rp.sip.SipMsg");
    private static Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private final static Map<String, HostCallBack> FUTURES = new ConcurrentHashMap<String, HostCallBack>();
    private volatile Lock lock = new ReentrantLock();
    private volatile Condition condition = lock.newCondition();
    private boolean isShortConnection;

    private volatile CountDownLatch latch = new CountDownLatch(1);

    private Channel channel = null;

    private volatile ByteBuf responseByteBuf = null;

    private long timeout;

    private long startTime = System.currentTimeMillis();

    public HostCallBack(Channel channel, long timeout) {
        this.channel = channel;
        this.timeout = timeout;
        this.isShortConnection = true;
        HostCallBack.FUTURES.put(this.channel.id().asLongText(), this);
    }

    public HostCallBack(String associationId, long timeout, boolean isShortConnection) {
        this.timeout = timeout;
        this.isShortConnection = isShortConnection;
        HostCallBack.FUTURES.put(associationId, this);
    }

    public static void receive(String channelId, ByteBuf msg) {
        HostCallBack future = HostCallBack.FUTURES.remove(channelId);
        if (future != null) {
            Lock lock = future.lock;
            try {
                lock.lockInterruptibly();
                RouteReceiveMessageHandler handler = getReceiveMessageHandler();
                if (handler != null) {
                    ByteBuf result = handler.unpackMessagePreprocess(msg);
                    if (result == null) {
                        return;
                    }
                    future.setResponseByteBuf(result);
                } else {
                    future.setResponseByteBuf(msg);
                }
                //  future.latch.countDown();
                future.condition.signal();
            } catch (InterruptedException e) {
                CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
                CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, e);
            } finally {
                lock.unlock();
            }
        }
    }

    private void setResponseByteBuf(ByteBuf responseByteBuf) {
        this.responseByteBuf = responseByteBuf;
    }

    public ByteBuf getResponseByteBuf() throws InterruptedException {
        try {
            lock.lockInterruptibly();
            while (responseByteBuf == null) {
                condition.await(this.timeout, TimeUnit.SECONDS);
                // latch.await(this.timeout, TimeUnit.SECONDS);
                if ((System.currentTimeMillis() - startTime) > timeout) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
            CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, e);
            return null;
        } finally {
            if (this.isShortConnection) {
                this.channel.close();
            }
            lock.unlock();
        }
        return responseByteBuf;
    }

    public static RouteReceiveMessageHandler getReceiveMessageHandler() {
        return (RouteReceiveMessageHandler) SpringBeanUtils.UTILS.getSpringBeanById("routeReceiveMessageHandler");
    }

    public interface RouteReceiveMessageHandler {
        ByteBuf unpackMessagePreprocess(ByteBuf response);

    }
}
