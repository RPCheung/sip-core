package com.rp.sip.rmi;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by cheungrp on 18/2/5.
 */
public interface SipBootstrapController extends Remote {

    void shutdown() throws RemoteException,NotBoundException;

    void restart() throws RemoteException;
}
