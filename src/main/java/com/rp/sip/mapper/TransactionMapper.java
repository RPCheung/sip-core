package com.rp.sip.mapper;

import com.rp.sip.component.ITransaction;
import com.rp.sip.handlers.TransactionMappingHandler;
import com.rp.sip.handlers.impl.DefaultTransactionMappingHandler;
import com.rp.sip.component.MessageObject;

import com.rp.sip.utils.DBUtils;
import com.rp.sip.utils.SpringBeanFactory;
import com.rp.sip.utils.SpringBeanUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by cheungrp on 17/11/28.
 */
public class TransactionMapper extends MessageToMessageDecoder<Map<String, MessageObject>> {


    public TransactionMapper() {

    }

    @Override
    protected void decode(ChannelHandlerContext ctx, Map<String, MessageObject> txEntry, List<Object> out) throws Exception {
        TransactionMappingHandler handler;
        String txCode = txEntry.keySet().iterator().next();
        MessageObject msg = txEntry.remove(txCode);
        if (SpringBeanFactory.getApplicationContext().containsBean("customTransactionMappingHandler")) {
            handler = (TransactionMappingHandler) SpringBeanFactory.getApplicationContext().getBean("customTransactionMappingHandler");
            handler.setTxCode(txCode);
            ITransaction transaction = handler.mappingTransactionByTxCode(msg);
            handler.createRouteMessageAndInit();
            if (transaction != null) {
                transaction.setRequestMessage(msg);
                transaction.setResponseMessage(handler.createResponseMessage());
                Map<String, Object> context = new ConcurrentHashMap<>(64);
                transaction.setTransactionContext(context);
                transaction.setSqlSessionFactory(DBUtils.UTILS.getUserSqlSessionFactory());
                out.add(transaction);
            }
            return;
        }
        handler = new DefaultTransactionMappingHandler();
        handler.setTxCode(txCode);
        ITransaction transaction = handler.mappingTransactionByTxCode(msg);
        handler.createRouteMessageAndInit();
        transaction.setRequestMessage(msg);
        transaction.setResponseMessage(handler.createResponseMessage());
        Map<String, Object> context = new ConcurrentHashMap<>(64);
        transaction.setTransactionContext(context);
        transaction.setSqlSessionFactory(DBUtils.UTILS.getUserSqlSessionFactory());
        out.add(transaction);
    }

}
