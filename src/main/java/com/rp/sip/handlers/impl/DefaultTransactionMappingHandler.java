package com.rp.sip.handlers.impl;

import com.rp.sip.component.ITransaction;
import com.rp.sip.component.impl.DefaultTransaction;
import com.rp.sip.db.mapper.SipSettingDAO;
import com.rp.sip.db.mapper.SipTranDAO;
import com.rp.sip.handlers.TransactionMappingHandler;
import com.rp.sip.component.MessageObject;
import com.rp.sip.message.DefaultMessageObject;
import com.rp.sip.utils.ClassLoadUtils;
import com.rp.sip.utils.CommonUtils;
import com.rp.sip.utils.MsgUtils;
import com.rp.sip.utils.SpringBeanUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by cheungrp on 17/11/29.
 */
public class DefaultTransactionMappingHandler implements TransactionMappingHandler {

    private String txCode;
    private ITransaction transaction;

    private Logger loggerMsg = LogManager.getLogger("com.rp.sip.SipMsg");
    private Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public ITransaction mappingTransactionByTxCode(MessageObject messageObject) {

        // ITransaction
        this.transaction = new DefaultTransaction(this.txCode);
        transaction.setRequestMessage(messageObject);

        return transaction;
    }

    @Override
    public MessageObject createResponseMessage() {
        MessageObject messageObject;
        try {
            String host = (String) getSettings().get("host");
            String txCode = transaction.getTxCode();
            Map<String,Object> tran = getTran(host,txCode);
            String resMsgClass = (String) tran.get("res_msg_class");
           Object o = ClassLoadUtils.utils.createSipUserObject(resMsgClass);
            messageObject = new DefaultMessageObject(JXPathContext.newContext(o));
        } catch (ClassNotFoundException e) {
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
            CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, e);
            return null;
        }

        return messageObject;
    }

    @Override
    public void setTxCode(String txCode) {
        this.txCode = txCode;
    }

    private Map<String, Object> getSettings() {
        SipSettingDAO settingDAO = SpringBeanUtils.UTILS.getSpringBeanByType(SipSettingDAO.class);
        return settingDAO.querySetting();
    }

    private Map<String, Object> getTran(String host, String txCode) {
        SipTranDAO sipTranDAO = SpringBeanUtils.UTILS.getSpringBeanByType(SipTranDAO.class);
        return sipTranDAO.queryTranByTxCode(host, txCode);
    }



}
