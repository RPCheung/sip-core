package com.rp.sip;

import com.rp.sip.rmi.SipBootstrapController;
import com.rp.sip.route.SipRouteClient;
import com.rp.sip.server.TCPServer;
import com.rp.sip.utils.SpringBeanUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


/**
 * Created by RP on 2017/5/2.
 */
public class SipBootstrap extends UnicastRemoteObject implements SipBootstrapController {

    private static final long serialVersionUID = 1L;

    private final static String SIP_HOME = System.getenv("SIP_HOME");

    private static Registry registry;


    static {
        System.setProperty("sip.home", SIP_HOME);
    }

    public SipBootstrap() throws RemoteException {
        super();
    }

    @Override
    public void shutdown() throws RemoteException, NotBoundException {
        SpringBeanUtils.UTILS.closeContext();
        registry.unbind("controller");
        System.exit(0);
    }

    @Override
    public void restart() throws RemoteException {
        SpringBeanUtils.UTILS.refreshContext();
    }

    public static void main(String[] args) throws RemoteException {

        SipBootstrap startup = new SipBootstrap();
        registrySipBootstrapController(startup);
        startup.startup();

    }

    private static void registrySipBootstrapController(SipBootstrapController controller) throws RemoteException {
        String CLOSE_PORT = System.getenv("CLOSE_PORT");
        registry = LocateRegistry.createRegistry(Integer.parseInt(CLOSE_PORT));
        registry.rebind("controller", controller);
    }

    private void startup() {

        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");

        TCPServer tcpServer = TCPServer.class.cast(ctx.getBean("nettyServer"));

        tcpServer.startup();
    }

}
