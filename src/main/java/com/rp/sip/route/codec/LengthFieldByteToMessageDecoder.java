package com.rp.sip.route.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.util.List;


/**
 * Created by cheungrp on 17/7/28.
 */
public class LengthFieldByteToMessageDecoder extends ByteToMessageDecoder {

    private Logger loggerMsg = LogManager.getLogger("com.rp.sip.SipMsg");
    private Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private int lengthFieldLength;

    private int lengthFieldOffset;

    private String charset;

    public LengthFieldByteToMessageDecoder(int lengthFieldLength, int lengthFieldOffset, String charset) {
        this.lengthFieldLength = lengthFieldLength;
        this.lengthFieldOffset = lengthFieldOffset;
        this.charset = charset;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {

        long length = msg.retainedSlice(lengthFieldOffset, lengthFieldLength).readLong(); //引用增加一次

        logger.info("route receive length:" + length);
        loggerMsg.info("route receive length:" + length);

        logger.info("route receive msg:" + msg.toString(Charset.forName(charset)));
        loggerMsg.info("route receive msg:" + msg.toString(Charset.forName(charset)));

        msg.release(); // 减少一次引用

        if (msg.discardReadBytes().readableBytes() != length + lengthFieldLength) {
            return;
        }

        msg.skipBytes(lengthFieldLength);
        out.add(msg.readBytes((int) length));

    }
}
