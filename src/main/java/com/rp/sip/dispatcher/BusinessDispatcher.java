package com.rp.sip.dispatcher;

import com.rp.sip.component.ITransaction;
import com.rp.sip.component.UserComponent;
import com.rp.sip.component.impl.UserComponentImpl;
import com.rp.sip.db.mapper.SipSettingDAO;
import com.rp.sip.db.mapper.SipTranDAO;
import com.rp.sip.handlers.BusinessDispatcherHandler;
import com.rp.sip.component.BusinessProcessor;
import com.rp.sip.utils.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by cheungrp on 17/10/30.
 */
public class BusinessDispatcher extends MessageToMessageDecoder<ITransaction> {

    private Logger loggerMsg = LogManager.getLogger("com.rp.sip.SipMsg");
    private Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());


    private String charset;

    public BusinessDispatcher(String charset) {
        this.charset = charset;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ITransaction transaction, List<Object> out) throws Exception {
        if (SpringBeanUtils.UTILS.isContainsBean4Id("customBusinessDispatcherHandler")) {
            BusinessDispatcherHandler dispatcherHandler = (BusinessDispatcherHandler) SpringBeanUtils.UTILS.getSpringBeanById("customBusinessDispatcherHandler");
            BusinessProcessor processor = dispatcherHandler.dispatcherHandle(transaction);
            //
            UserComponent component = new UserComponentImpl(transaction);
            processor.setUserComponent(component);
            out.add(processor);
        } else {
            String host = (String) getSettings().get("host");
            String txCode = transaction.getTxCode();
            Map<String, Object> tran = getTran(host, txCode);
            String processorClass = (String) tran.get("business_processor_class");

            BusinessProcessor processor = (BusinessProcessor) ClassLoadUtils.utils.createSipUserObject(processorClass);
            if (processor == null) {
                throw new NullPointerException("找不到 此交易业务处理器");
            }

            UserComponent component = new UserComponentImpl(transaction);
            processor.setUserComponent(component);

            Map<String, BusinessProcessor> processorEntry = new ConcurrentHashMap<>(1);
            processorEntry.put(txCode, processor);
            out.add(processorEntry);
        }
    }

    private Map<String, Object> getSettings() {
        SipSettingDAO settingDAO = SpringBeanUtils.UTILS.getSpringBeanByType(SipSettingDAO.class);
        return settingDAO.querySetting();
    }


    private Map<String, Object> getTran(String host, String txCode) {
        SipTranDAO sipTranDAO = SpringBeanUtils.UTILS.getSpringBeanByType(SipTranDAO.class);
        return sipTranDAO.queryTranByTxCode(host, txCode);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        CommonUtils.getCommonUtils().printExceptionFormat(logger, cause);
        CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("与 [" + ctx.channel().remoteAddress() + "] 关闭了连接");
        loggerMsg.info("与 [" + ctx.channel().remoteAddress() + "] 关闭了连接");
        super.channelInactive(ctx);
    }
}
