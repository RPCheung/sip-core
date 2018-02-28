package com.rp.sip.route;

import com.rp.sip.component.MessageObject;
import org.apache.http.Header;

/**
 * Created by cheungrp on 18/2/9.
 */
public interface IRoute {
    MessageObject sendAndReceiveMsg4User(MessageObject messageObject) throws InterruptedException;

    /**
     * 4 tcp
     *
     * @param associationId
     */
    void setAssociationId(String associationId);

    /**
     * 4 http
     * @param key
     * @param value
     */
    void addHttpRequestHeader(String key, String value);


    Header[] getResponseAllHeaders();



}
