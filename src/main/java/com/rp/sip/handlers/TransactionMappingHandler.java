package com.rp.sip.handlers;

import com.rp.sip.component.ITransaction;
import com.rp.sip.component.MessageObject;
import com.rp.sip.route.IRoute;

/**
 * Created by cheungrp on 17/11/28.
 */
public interface TransactionMappingHandler {

    IRoute createRouteMessageAndInit();

    ITransaction mappingTransactionByTxCode(MessageObject object);

    MessageObject createResponseMessage();

    void setTxCode(String txCode);

}
