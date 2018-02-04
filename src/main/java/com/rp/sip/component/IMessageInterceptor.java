package com.rp.sip.component;

/**
 * Created by cheungrp on 18/1/31.
 */
public interface IMessageInterceptor {

    MessageObject beforeMarshal(MessageObject messageObject);

    byte[] afterMarshal(byte[] messageObject);

    byte[] beforeUnmarshal(byte[] messageObject);

    MessageObject afterUnmarshal(MessageObject messageObject);
}
