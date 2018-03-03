package com.rp.sip.utils;

import java.io.File;

/**
 * Created by cheungrp on 18/3/2.
 */
public interface SIPPath {
    String JAR_PATH = System.getenv("SIP_HOME") +
            File.separator + "work" + File.separator + "user" + File.separator + "lib";
    String SYSTEM_PATH = System.getenv("SIP_HOME") +
            File.separator + "work" + File.separator + "sip" + File.separator + "lib";
    String BIZ_PATH = System.getenv("SIP_HOME") +
            File.separator + "work" + File.separator + "user" + File.separator + "deployed";
    String BIZ_BACKUPS_PATH = System.getenv("SIP_HOME") +
            File.separator + "work" + File.separator + "user" + File.separator + "rollback-backups";
    String DEPLOYABLE_PATH = System.getenv("SIP_HOME") + File.separator + "deployable";
    String JDK_COMMAND_PATH = System.getenv("JAVA_HOME") + File.separator + "bin";
}
