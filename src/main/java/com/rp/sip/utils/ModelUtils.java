package com.rp.sip.utils;

import com.rp.sip.component.MessageObject;
import com.rp.sip.component.MessageType;
import com.rp.sip.model.MessageModel;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Created by cheungrp on 17/12/8.
 */
public enum ModelUtils {
    UTILS;
    private MessageModel messageModel;

    ModelUtils() {
    }

    public MessageModel createMessageModel(MessageType messageType) {
        if(messageModel == null){
            messageModel = new MessageModel(messageType);
        }
        return messageModel;
    }

    public MessageModel getMessageModel() {
        if (messageModel != null) {
            return messageModel;
        }
        return null;
    }
}
