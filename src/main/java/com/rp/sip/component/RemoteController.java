package com.rp.sip.component;

import java.rmi.Remote;

/**
 * Created by cheungrp on 18/2/27.
 */
public interface RemoteController extends Remote {

    void start() throws Exception;

    void restart() throws Exception;

    void shutdown() throws Exception;
}
