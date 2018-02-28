package com.rp.sip.route.handlers;

import io.netty.channel.ChannelPipeline;

/**
 * Created by cheungrp on 18/2/9.
 */
public interface PoolChannelHandler {
    void addHandlers(ChannelPipeline channelPipeline);
}
