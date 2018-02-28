package com.rp.sip.db.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * Created by cheungrp on 18/2/22.
 */
public interface RoutePoolSettingDAO {
    @Select("select * from sip_route_pool_setting")
    Map<String, Object> queryRoutePoolSetting();
}
