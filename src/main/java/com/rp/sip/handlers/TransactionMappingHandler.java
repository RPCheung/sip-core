package com.rp.sip.handlers;

import com.rp.sip.component.ITransaction;
import com.rp.sip.component.MessageObject;
import com.rp.sip.model.MessageModel;
import com.rp.sip.model.TransactionModel;
import com.rp.sip.route.SipRouteClient;

/**
 * Created by cheungrp on 17/11/28.
 */
public interface TransactionMappingHandler {

    void createRouteMessageAndInit();

    ITransaction mappingTransactionByTxCode(MessageObject object);

    MessageObject createResponseMessage();

    void setTxCode(String txCode);

}
