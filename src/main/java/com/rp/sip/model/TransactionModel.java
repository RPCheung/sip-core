package com.rp.sip.model;

import com.rp.sip.component.ITransaction;

import java.io.Serializable;

/**
 * Created by cheungrp on 17/10/30.
 */
public class TransactionModel implements Serializable {

    private ITransaction transaction;
    private MessageModel messageModel;
    private String txCode;

    public ITransaction getTransaction() {
        return transaction;
    }

    public void setTransaction(ITransaction transaction) {
        this.transaction = transaction;
    }

    public MessageModel getMessageModel() {
        return messageModel;
    }

    public void setMessageModel(MessageModel messageModel) {
        this.messageModel = messageModel;
    }

    public String getTxCode() {
        return txCode;
    }

    public void setTxCode(String txCode) {
        this.txCode = txCode;
    }
}
