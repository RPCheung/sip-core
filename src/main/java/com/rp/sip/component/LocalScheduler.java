package com.rp.sip.component;

import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import java.util.Date;

/**
 * Created by cheungrp on 18/2/27.
 */
public interface LocalScheduler {

    void start() throws SchedulerException;

    Date scheduleJob(JobDetail job, Trigger trigger) throws SchedulerException;

    void shutdown() throws SchedulerException;
}
