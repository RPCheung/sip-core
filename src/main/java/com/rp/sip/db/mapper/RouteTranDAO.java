package com.rp.sip.db.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * Created by cheungrp on 18/2/4.
 */
public interface RouteTranDAO {

    @Select("select * from sip_route_tran where route_tran_id=#{route_tran_id}")
    Map<String, Object> queryTran(@Param(value = "route_tran_id") String routeTranId);

}
