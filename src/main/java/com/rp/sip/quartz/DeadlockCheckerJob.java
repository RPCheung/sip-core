package com.rp.sip.quartz;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.lang.invoke.MethodHandles;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.List;

/**
 * Created by cheungrp on 18/2/27.
 */
public class DeadlockCheckerJob implements Job {

    private Logger loggerMsg = LogManager.getLogger("com.rp.sip.deadlockCheckerMsg");
    private Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private static final ThreadMXBean TMX = ManagementFactory.getThreadMXBean();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        checkDeadLock(context.getJobDetail().getJobDataMap().getBoolean("isTryBreakDeadLock"));

    }

    private ThreadInfo[] findDeadLockThreadInfo() {
        long[] ids = TMX.findDeadlockedThreads();
        return null == TMX.findDeadlockedThreads() ? new ThreadInfo[0] : TMX.getThreadInfo(ids);
    }

    private boolean checkDeadLock(boolean isTryBreakDeadLock) {
        List<ThreadInfo> threadInfos = Arrays.asList(findDeadLockThreadInfo());
        if (threadInfos.size() != 0) {
            for (ThreadInfo info : threadInfos) {
                long threadId = info.getThreadId();
                String threadName = info.getThreadName();
                logger.info("存在死锁:  threadName:" + threadName + "   threadId" + threadId);
                loggerMsg.info("存在死锁:  threadName:" + threadName + "   threadId" + threadId);
                return isTryBreakDeadLock && interruptThread(threadId);
            }
        } else {
            logger.info("未检测出死锁线程.");
            loggerMsg.info("未检测出死锁线程.");
            return true;
        }
        return true;
    }

    private Thread findThreadById(long threadId) {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getId() == threadId) {
                return t;
            }
        }
        return null;
    }

    private boolean interruptThread(long threadId) {
        Thread t = findThreadById(threadId);
        if (t != null) {
            t.interrupt();
            logger.info("已发送中断.");
            loggerMsg.info("已发送中断.");
            return true;
        }
        logger.info("线程不存在.");
        loggerMsg.info("线程不存在.");
        return false;
    }


}
