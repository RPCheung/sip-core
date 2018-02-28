package com.rp.sip.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.List;

/**
 * Created by cheungrp on 17/6/29.
 */

public class LengthFieldPrepender extends MessageToMessageEncoder<ByteBuf> {

    private Logger loggerMsg = LogManager.getLogger("com.rp.sip.SipMsg");
    private Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private int lengthFieldLength;

    private boolean lengthIncludesLengthFieldLength;

    private String charset;

    public LengthFieldPrepender(int lengthFieldLength, boolean lengthIncludesLengthFieldLength, String charset) {
        this.lengthFieldLength = lengthFieldLength;
        this.lengthIncludesLengthFieldLength = lengthIncludesLengthFieldLength;
        this.charset = charset;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        int contentLength = msg.writerIndex();
        if (msg.writerIndex() == 0) {
            return;
        }
        if (lengthIncludesLengthFieldLength) {
            contentLength = contentLength + lengthFieldLength;
        }

        NumberFormat format = NumberFormat.getInstance();
        format.setMinimumIntegerDigits(lengthFieldLength);
        format.setGroupingUsed(false);
        byte[] formattedLengthBytes = format.format(contentLength).getBytes(this.charset);
        ByteBuf newContentBuf = Unpooled.buffer(this.lengthFieldLength + contentLength);
        newContentBuf.writeBytes(formattedLengthBytes, 0, formattedLengthBytes.length);
        msg.readBytes(newContentBuf);
        out.add(newContentBuf);
        logger.info("server response msg: " + newContentBuf.toString(Charset.forName(this.charset)));
        loggerMsg.info("server response msg: " + newContentBuf.toString(Charset.forName(this.charset)));

    }

}
