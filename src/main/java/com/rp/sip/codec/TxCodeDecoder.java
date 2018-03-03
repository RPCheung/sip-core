package com.rp.sip.codec;

import com.rp.sip.classloader.SipUserClassloader;
import com.rp.sip.component.MessageType;
import com.rp.sip.db.mapper.SipSettingDAO;
import com.rp.sip.db.mapper.SipTranDAO;
import com.rp.sip.handlers.FindTxCodeHandler;

import com.rp.sip.model.SIPInfo;
import com.rp.sip.utils.SpringBeanUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by cheungrp on 17/10/28.
 */
public class TxCodeDecoder extends MessageToMessageDecoder<ByteBuf> {

    private Logger loggerMsg = LogManager.getLogger("com.rp.sip.SipMsg");
    private Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private String charset;
    private MessageType messageType;

    public TxCodeDecoder(String charset, MessageType messageType) {
        this.charset = charset;
        this.messageType = messageType;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {

        logger.info("request msg:" + msg.toString(Charset.forName(charset)));
        loggerMsg.info("request msg:" + msg.toString(Charset.forName(charset)));

        // mybatis
        SipSettingDAO settingDAO = SpringBeanUtils.UTILS.getSpringBeanByType(SipSettingDAO.class);
        SipTranDAO tranDAO = SpringBeanUtils.UTILS.getSpringBeanByType(SipTranDAO.class);

        FindTxCodeHandler findTxCodeHandler;
        if (SpringBeanUtils.UTILS.isContainsBean4Id("customMessagePacker")) {
            findTxCodeHandler = (FindTxCodeHandler) SpringBeanUtils.UTILS.getSpringBeanById("customFindTxCodeHandler");
        } else {
            findTxCodeHandler = (FindTxCodeHandler) SpringBeanUtils.UTILS.getSpringBeanById("defaultFindTxCodeHandler");
        }

        // 从数据库 获取 msgType
        MessageType messageType = this.messageType;
        // 打解包  是为了 让 ByteBuf 转换为 MessageObject
        SIPInfo info = (SIPInfo) SpringBeanUtils.UTILS.getSpringBeanById("sip-info");
        Map<String, Object> setting = settingDAO.querySetting(info.getServerId());
        String txCodePath = (String) setting.get("txCodePath");
        logger.info("交易码路径:" + txCodePath);
        findTxCodeHandler.setTxCodePath(txCodePath);
        ByteBuf message = Unpooled.copiedBuffer(msg);
        String txCode = findTxCodeHandler.findTxCodeFromReqMsg(msg, messageType, setting);
        String host = (String) setting.get("host");
        Map<String, Object> tran = tranDAO.queryTranByTxCode(host, txCode);
        if (tran == null) {
            throw new RuntimeException("not found txCode");
        }
        Map<String, ByteBuf> txEntry = new ConcurrentHashMap<>(1);
        txEntry.put((String) tran.get("txCode"), message);
        out.add(txEntry);
    }


}
