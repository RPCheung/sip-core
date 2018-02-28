package com.rp.sip.codec;

import com.rp.sip.codec.DecoderMessageState;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.util.CharsetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.util.List;

import static com.rp.sip.codec.DecoderMessageState.READ_LENGTH;

/**
 * Created by cheungrp on 17/9/15.
 */
public class SipReplayingDecoder extends ReplayingDecoder<DecoderMessageState> {

    private Logger loggerMsg = LogManager.getLogger("com.rp.sip.SipMsg");
    private Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private int length1;

    private int length;

    private boolean lengthIncludesLengthFieldLength;

    public SipReplayingDecoder(int length, boolean lengthIncludesLengthFieldLength) {
        super(READ_LENGTH);
        this.length = length;
        this.lengthIncludesLengthFieldLength = lengthIncludesLengthFieldLength;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("与 [" + ctx.channel().remoteAddress() + "] 创建了连接");
        loggerMsg.info("与 [" + ctx.channel().remoteAddress() + "] 创建了连接");
        super.channelActive(ctx);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx,
                          ByteBuf buf, List<Object> out) throws Exception {

        if (buf.readableBytes() < this.length) return;

        switch (state()) {
            case READ_LENGTH:
                ByteBuf lengthBuf = buf.readBytes(this.length);
                length1 = lengthIncludesLengthFieldLength ?
                        (Integer.parseInt(lengthBuf.toString(CharsetUtil.UTF_8)) - length) :
                        Integer.parseInt(lengthBuf.toString(CharsetUtil.UTF_8));
                lengthBuf.release();
                checkpoint(DecoderMessageState.READ_CONTENT);
            case READ_CONTENT:
                ByteBuf frame = buf.readBytes(length1);
                ByteBuf contentBuf = Unpooled.buffer();
                contentBuf.writeLong(length1);
                contentBuf.writeBytes(frame);
                frame.release();

                checkpoint(READ_LENGTH);
                out.add(contentBuf);
                break;
            default:
                throw new Error("Shouldn't reach here.");
        }
    }

}
