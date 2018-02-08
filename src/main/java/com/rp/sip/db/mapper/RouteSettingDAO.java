package com.rp.sip.db.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * Created by cheungrp on 18/2/4.
 */
public interface RouteSettingDAO {

    @Select("select * from sip_route_setting where route_id=#{route_id}")
    Map<String, Object> querySetting(@Param(value = "route_id") String routeId);
}
