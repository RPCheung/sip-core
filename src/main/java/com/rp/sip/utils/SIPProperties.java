package com.rp.sip.utils;

import com.rp.sip.db.mapper.*;
import com.rp.sip.model.SIPInfo;

import java.util.Map;

/**
 * Created by cheungrp on 18/3/17.
 */
public enum SIPProperties {

    properties;

    public Map<String, Object> getTran(String txCode) {
        SIPInfo info = (SIPInfo) SpringBeanUtils.UTILS.getSpringBeanById("sip-info");
        SipTranDAO sipTranDAO = SpringBeanUtils.UTILS.getSpringBeanByType(SipTranDAO.class);
        return sipTranDAO.queryTranByTxCode(info.getServerId(), txCode);
    }

    public Map<String, Object> getSetting() {
        SipSettingDAO settingDAO = SpringBeanUtils.UTILS.getSpringBeanByType(SipSettingDAO.class);
        SIPInfo info = (SIPInfo) SpringBeanUtils.UTILS.getSpringBeanById("sip-info");
        return settingDAO.querySetting(info.getServerId());
    }

    public Map<String, Object> getRouteTran(String txCode) {
        String routeTranId = (String) getTran(txCode).get("route_tran_id");
        RouteTranDAO routeTranDAO = SpringBeanUtils.UTILS.getSpringBeanByType(RouteTranDAO.class);
        return routeTranDAO.queryTran(routeTranId);
    }

    public Map<String, Object> getRouteSetting(String txCode) {
        RouteSettingDAO routeSetting = SpringBeanUtils.UTILS.getSpringBeanByType(RouteSettingDAO.class);
        String routeId = (String) getRouteTran(txCode).get("route_id");
        return routeSetting.querySetting(routeId);
    }

    public Map<String, Object> getRoutePoolSetting() {
        RoutePoolSettingDAO routePoolSetting = SpringBeanUtils.UTILS.getSpringBeanByType(RoutePoolSettingDAO.class);
        return routePoolSetting.queryRoutePoolSetting();
    }
}
