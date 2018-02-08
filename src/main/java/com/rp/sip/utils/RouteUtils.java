package com.rp.sip.utils;

import com.rp.sip.route.SipRouteClient;

/**
 * Created by cheungrp on 18/2/7.
 */
public enum RouteUtils {

    UTILS;

    public SipRouteClient getCurrentRoute() {
        return (SipRouteClient) SpringBeanUtils.UTILS.getSpringBeanById("sipRouteClient");
    }


}
