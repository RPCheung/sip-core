package com.rp.sip.route;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

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

    private final static Map<String, HostCallBack> FUTURES = new ConcurrentHashMap<String, HostCallBack>();
    private volatile Lock lock = new ReentrantLock();
    private volatile Condition condition = lock.newCondition();


    private volatile CountDownLatch latch = new CountDownLatch(1);

    private Channel channel = null;

    private volatile ByteBuf responseByteBuf = null;

    private long timeout;

    public HostCallBack(Channel channel, long timeout) {
        this.channel = channel;
        this.timeout = timeout;
        HostCallBack.FUTURES.put(this.channel.id().asLongText(), this);
    }

    public static void receive(String channelId, ByteBuf msg) {
        msg.nioBuffer();
        HostCallBack future = HostCallBack.FUTURES.remove(channelId);
        if (future != null) {
            Lock lock = future.lock;
            try {
                lock.lockInterruptibly();
                future.setResponseByteBuf(msg);
                //  future.latch.countDown();
                future.condition.signal();
            } catch (InterruptedException e) {
                e.printStackTrace();
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
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } finally {
            this.channel.close();
            lock.unlock();
        }
        return responseByteBuf;
    }
}
