package com.rp.sip.route;


import com.rp.sip.component.MessageObject;
import com.rp.sip.utils.CommonUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
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

    private volatile MessageObject response = null;

    private long timeout;

    public HostCallBack(Channel channel, long timeout) {
        this.channel = channel;
        this.timeout = timeout;
        this.isShortConnection = true;
        HostCallBack.FUTURES.put(this.channel.id().asLongText(), this);
    }

    HostCallBack(String associationId, long timeout) {
        this.timeout = timeout;
        this.isShortConnection = false;
        HostCallBack.FUTURES.put(associationId, this);
    }

    public static void receive(String associationId, MessageObject msg) {
        HostCallBack future = HostCallBack.FUTURES.remove(associationId);
        if (future != null) {
            ReentrantLock lock = future.lock;
            try {
                lock.lockInterruptibly();
                future.setResponseMessageObject(msg);
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

    private void setResponseMessageObject(MessageObject response) {
        this.response = response;
    }

    MessageObject getResponseMessageObject() throws InterruptedException {
        boolean interrupted = true;
        try {
            lock.lockInterruptibly();
            while (response == null) {
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
        return response;
    }

    public interface RouteReceiveMessageHandler {
        // 实现时 要手动释放 ByteBuf
        ByteBuf unpackMessagePreprocess(ByteBuf response);
    }
}
