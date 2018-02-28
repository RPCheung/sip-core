package com.rp.sip.handlers.impl;

import com.rp.sip.codec.SipReplayingDecoder;
import com.rp.sip.component.*;
import com.rp.sip.component.impl.DefaultTransaction;
import com.rp.sip.db.mapper.*;
import com.rp.sip.handlers.TransactionMappingHandler;
import com.rp.sip.message.DefaultMessageObject;
import com.rp.sip.route.HostCallBack;
import com.rp.sip.route.HttpRoute;
import com.rp.sip.route.IRoute;
import com.rp.sip.route.TcpRoute;
import com.rp.sip.route.codec.LengthFieldByteToMessageDecoder;
import com.rp.sip.route.codec.LengthFieldPrepender;
import com.rp.sip.route.handlers.HttpTrustedHandler;
import com.rp.sip.route.handlers.ReceiveMsgHandler;
import com.rp.sip.route.packer.PackMessage;
import com.rp.sip.utils.ClassLoadUtils;
import com.rp.sip.utils.CommonUtils;
import com.rp.sip.utils.MsgUtils;
import com.rp.sip.utils.SpringBeanUtils;
import io.netty.buffer.ByteBuf;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Created by cheungrp on 17/11/29.
 */
public class DefaultTransactionMappingHandler implements TransactionMappingHandler {

    private String txCode;
    private ITransaction transaction;

    private Logger loggerMsg = LogManager.getLogger("com.rp.sip.SipMsg");
    private Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public IRoute createRouteMessageAndInit() {

        Map<String, Object> result = getRouteTran();
        String reqMsgClass = (String) result.get("req_msg_class");
        String resMsgClass = (String) result.get("res_msg_class");

        // init IRoute();
        switch (ProtocolType.valueOf((String) getRouteTran().get("routeProtocol"))) {
            case TCP: {
                initTcpRoute(reqMsgClass, resMsgClass);
                return null;
            }
            case HTTP: {
                initHttpRoute(reqMsgClass, resMsgClass);
                return null;
            }
            default: {
                return null;
            }
        }
    }

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
            Map<String, Object> tran = getTran(host, txCode);
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

    private Map<String, Object> getRouteSetting() {
        RouteSettingDAO routeSetting = SpringBeanUtils.UTILS.getSpringBeanByType(RouteSettingDAO.class);
        String routeId = (String) getRouteTran().get("route_id");
        return routeSetting.querySetting(routeId);
    }

    private Map<String, Object> getRouteTran() {
        String host = (String) getSettings().get("host");
        String routeTranId = (String) getTran(host, this.txCode).get("route_tran_id");
        RouteTranDAO routeTranDAO = SpringBeanUtils.UTILS.getSpringBeanByType(RouteTranDAO.class);
        return routeTranDAO.queryTran(routeTranId);
    }

    private MessageObject createRouteMessage(String className) throws ClassNotFoundException {
        return new DefaultMessageObject(JXPathContext.newContext(ClassLoadUtils.utils.createSipUserObject(className)));
    }

    private PackMessage createRoutePackMessage(Map<String, Object> result, String resMsgClass) {
        PackMessage packMessage = null;

        String packMessageClassName = (String) result.get("packMessage");

        if (packMessageClassName != null) {
            try {
                packMessage = (PackMessage) ClassLoadUtils.utils.createSipUserObject(packMessageClassName);
            } catch (ClassNotFoundException e) {
                CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
            }
        }

        if (packMessage == null) {
            packMessage = new PackMessage() {
                @Override
                public MessageObject unpackMessage(ByteBuf response) {
                    if (response == null) {
                        return null;
                    }
                    return new DefaultMessageObject(JXPathContext.newContext(MsgUtils.UTILS.unpackMessage(MsgUtils.UTILS.byteBuf2Bytes(response), resMsgClass)));
                }

                @Override
                public ByteBuf packMessage(MessageObject request) {
                    if (request == null) {
                        return null;
                    }
                    return MsgUtils.UTILS.bytes2ByteBuf(MsgUtils.UTILS.packMessage(request.getSipMessagePojo()));
                }
            };
        }
        return packMessage;
    }

    private IMessageInterceptor hasMessageInterceptor(Map<String, Object> result) {

        String messageInterceptorClassName = (String) result.get("messageInterceptor");
        if (messageInterceptorClassName != null) {
            try {
                return (IMessageInterceptor) ClassLoadUtils.utils.createSipUserObject(messageInterceptorClassName);
            } catch (ClassNotFoundException e) {
                CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
                return null;
            }
        } else {
            return null;
        }
    }

    private void initTcpRoute(String reqMsgClass, String resMsgClass) {
        PackMessage packMessage = createRoutePackMessage(getRouteTran(), resMsgClass);
        IRoute route = new TcpRoute(Boolean.parseBoolean((String) getRouteSetting().get("isShortConnection")), new InetSocketAddress((String) getRouteSetting().get("route_host"), Integer.parseInt((String) getRouteSetting().get("route_port"))), channelPipeline -> {
            channelPipeline.addLast(new LengthFieldPrepender(Integer.parseInt((String) getRouteSetting().get("lengthFieldLength")),
                    Boolean.valueOf((String) getRouteSetting().get("lengthIncludesLengthFieldLength")),
                    (String) getRouteSetting().get("charset")));
            channelPipeline.addLast(new SipReplayingDecoder(Integer.parseInt((String) getRouteSetting().get("lengthFieldLength")),
                    Boolean.valueOf((String) getRouteSetting().get("lengthIncludesLengthFieldLength"))));
            channelPipeline.addLast(new LengthFieldByteToMessageDecoder(Integer.parseInt((String) getRouteSetting().get("lengthFieldLength")),
                    Integer.parseInt((String) getRouteSetting().get("lengthFieldOffset")),
                    (String) getRouteSetting().get("charset")));
            channelPipeline.addLast(new ReceiveMsgHandler());
        }, packMessage,
                hasMessageInterceptor(getRouteTran()),
                hasRouteReceiveMessageHandler(getRouteTran()),
                Long.parseLong((String) getRouteSetting().get("timeout")),
                Charset.forName((String) getRouteSetting().get("charset")));
        try {
            transaction.setRouteRequestMessage(createRouteMessage(reqMsgClass));
            transaction.setRoute(route);
        } catch (ClassNotFoundException e) {
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
        }
    }

    private void initHttpRoute(String reqMsgClass, String resMsgClass) {
        PackMessage packMessage = createRoutePackMessage(getRouteTran(), resMsgClass);
        IRoute route = new HttpRoute((String) getRouteSetting().get("uri"),
                packMessage,
                hasMessageInterceptor(getRouteTran()),
                HttpMethod.valueOf((String) getRouteSetting().get("httpMethod")),
                hasHttpTrustedHandler(getRouteTran()),
                Integer.parseInt((String) getRouteSetting().get("timeout")),
                Charset.forName((String) getRouteSetting().get("charset")));
        try {
            transaction.setRouteRequestMessage(createRouteMessage(reqMsgClass));
            transaction.setRoute(route);
        } catch (ClassNotFoundException e) {
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
        }
    }

    private HttpTrustedHandler hasHttpTrustedHandler(Map<String, Object> result) {
        String httpTrustedHandlerClassName = (String) result.get("trustedHandler");
        if (httpTrustedHandlerClassName != null) {
            try {
                return (HttpTrustedHandler) ClassLoadUtils.utils.createSipUserObject(httpTrustedHandlerClassName);
            } catch (ClassNotFoundException e) {
                CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
                return null;
            }
        } else {
            return null;
        }
    }

    private HostCallBack.RouteReceiveMessageHandler hasRouteReceiveMessageHandler(Map<String, Object> result) {
        String receiveMessageHandlerClassName = (String) result.get("receiveMessageHandler");
        if (receiveMessageHandlerClassName != null) {
            try {
                return (HostCallBack.RouteReceiveMessageHandler) ClassLoadUtils.utils.createSipUserObject(receiveMessageHandlerClassName);
            } catch (ClassNotFoundException e) {
                CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
                return null;
            }
        } else {
            return null;
        }
    }
}
