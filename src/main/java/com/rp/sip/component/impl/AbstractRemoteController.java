package com.rp.sip.component.impl;

import com.rp.sip.component.RemoteController;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by cheungrp on 18/2/27.
 */
public abstract class AbstractRemoteController extends UnicastRemoteObject implements RemoteController {

    protected AbstractRemoteController() throws RemoteException {
        super();
    }
}
