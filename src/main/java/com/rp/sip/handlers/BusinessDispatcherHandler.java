package com.rp.sip.handlers;

import com.rp.sip.component.BusinessProcessor;
import com.rp.sip.component.ITransaction;

/**
 * Created by cheungrp on 18/1/8.
 */
public interface BusinessDispatcherHandler {

    BusinessProcessor dispatcherHandle(ITransaction transaction);
}
