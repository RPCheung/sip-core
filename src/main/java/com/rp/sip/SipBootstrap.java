package com.rp.sip;

import com.rp.sip.server.TCPServer;
import org.apache.commons.io.IOUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;


/**
 * Created by RP on 2017/5/2.
 */
public class SipBootstrap {

    private final static String SIP_HOME = System.getenv("SIP_HOME");

    static {
        System.setProperty("sip.home", SIP_HOME);
    }

    public static void main(String[] args) {

        SipBootstrap bootstrap = new SipBootstrap();
        bootstrap.startup();
    }

    private void startup() {

        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");

        TCPServer tcpServer = TCPServer.class.cast(ctx.getBean("nettyServer"));

        tcpServer.startup();
    }


}
