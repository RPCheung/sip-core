package com.rp.sip.quartz;

import com.rp.sip.component.LocalScheduler;
import com.rp.sip.db.mapper.SipSettingDAO;
import com.rp.sip.model.SIPInfo;
import com.rp.sip.utils.SpringBeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

/**
 * Created by cheungrp on 18/2/27.
 */
@Component("deadlockChecker")
public class DeadlockChecker implements LocalScheduler {

    private Logger loggerMsg = LogManager.getLogger("com.rp.sip.deadlockCheckerMsg");
    private Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private final static String CHECK_THREAD_POOL_SIZE = "50";
    private final static String CHECK_THREAD_PRIORITY = "5";
    private final static String CHECK_JOD_CLASS = "org.quartz.simpl.RAMJobStore";
    private Scheduler scheduler = null;

    public void init() throws SchedulerException {
        Properties properties = new Properties();
        properties.setProperty("org.quartz.threadPool.threadCount", CHECK_THREAD_POOL_SIZE);
        properties.setProperty("org.quartz.threadPool.threadPriority", CHECK_THREAD_PRIORITY);
        properties.setProperty("org.quartz.jobStore.class", CHECK_JOD_CLASS);
        SchedulerFactory sf = new StdSchedulerFactory(properties);
        scheduler = sf.getScheduler();
        JobDetail job = JobBuilder.newJob(DeadlockCheckerJob.class).withDescription("this is deadlockChecker.")
                .withIdentity("deadlockChecker", "deadlockCheckerGroup").build();
        job.getJobDataMap().put("isTryBreakDeadLock", Boolean.parseBoolean((String) getSettings().get("isTryBreakDeadLock")));
        Trigger trigger = TriggerBuilder.newTrigger().startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().repeatForever().withIntervalInSeconds(1)).build();
        scheduler.scheduleJob(job, trigger);
    }

    @Override
    public void start() throws SchedulerException {
        logger.info("将启动死锁检查器.");
        loggerMsg.info("将启动死锁检查器.");
        scheduler.start();
    }

    @Override
    public Date scheduleJob(JobDetail job, Trigger trigger) throws SchedulerException {
        return null;
    }

    @Override
    public void shutdown() throws SchedulerException {
        logger.info("将关闭死锁检查器.");
        loggerMsg.info("将关闭死锁检查器.");
        scheduler.shutdown();
    }

    private Map<String, Object> getSettings() {
        SipSettingDAO settingDAO = SpringBeanUtils.UTILS.getSpringBeanByType(SipSettingDAO.class);
        SIPInfo info = (SIPInfo) SpringBeanUtils.UTILS.getSpringBeanById("sip-info");
        return settingDAO.querySetting(info.getServerId());
    }
}
