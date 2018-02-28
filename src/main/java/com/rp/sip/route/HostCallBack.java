package com.rp.sip.route;


import com.rp.sip.utils.CommonUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by cheungrp on 17/10/31.
 */

/**
 * 在使用锁的时候 必须固定写法为:
 * <p>
 * try {
 * lock.lockInterruptibly(); // 重点 （不能使用 lock.lock() 否则发生死锁将是不可逆的）
 * while () {
 * condition.await(this.timeout, TimeUnit.SECONDS);
 * }
 * } catch (InterruptedException e) {
 * return null; // 重点
 * } finally {
 * if (lock.isHeldByCurrentThread()) { // 重点
 * lock.unlock(); // 重点
 * }
 * }
 */
public class HostCallBack {

    private static Logger loggerMsg = LogManager.getLogger("com.rp.sip.SipMsg");
    private static Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private final static Map<String, HostCallBack> FUTURES = new ConcurrentHashMap<String, HostCallBack>();
    private volatile ReentrantLock lock = new ReentrantLock();
    private volatile Condition condition = lock.newCondition();
    private boolean isShortConnection;

    private volatile CountDownLatch latch = new CountDownLatch(1);

    private Channel channel = null;

    private RouteReceiveMessageHandler handler;

    private volatile ByteBuf responseByteBuf = null;

    private long timeout;

    public HostCallBack(Channel channel, long timeout, RouteReceiveMessageHandler handler) {
        this.channel = channel;
        this.timeout = timeout;
        this.isShortConnection = true;
        this.handler = handler;
        HostCallBack.FUTURES.put(this.channel.id().asLongText(), this);
    }

    HostCallBack(String associationId, long timeout, RouteReceiveMessageHandler handler) {
        this.timeout = timeout;
        this.isShortConnection = false;
        this.handler = handler;
        HostCallBack.FUTURES.put(associationId, this);
    }

    public static void receive(String associationId, ByteBuf msg) {
        HostCallBack future = HostCallBack.FUTURES.remove(associationId);
        if (future != null) {
            ReentrantLock lock = future.lock;
            try {
                lock.lockInterruptibly();
                RouteReceiveMessageHandler handler = getReceiveMessageHandler(future);
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
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
    }

    private void setResponseByteBuf(ByteBuf responseByteBuf) {
        this.responseByteBuf = responseByteBuf;
    }

    ByteBuf getResponseByteBuf() throws InterruptedException {
        boolean interrupted = true;
        try {
            lock.lockInterruptibly();
            while (responseByteBuf == null) {
                // latch.await(this.timeout, TimeUnit.SECONDS);
                if (!interrupted) {
                    Thread.currentThread().interrupt();
                }
                interrupted = condition.await(this.timeout, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
            CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, e);
            return null;
        } finally {
            if (this.isShortConnection) {
                this.channel.close();
            }
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return responseByteBuf;
    }

    private static RouteReceiveMessageHandler getReceiveMessageHandler(HostCallBack future) {
        return future.handler;
    }

    public interface RouteReceiveMessageHandler {
        ByteBuf unpackMessagePreprocess(ByteBuf response);
    }
}
