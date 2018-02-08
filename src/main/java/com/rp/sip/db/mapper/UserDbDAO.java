package com.rp.sip.db.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * Created by cheungrp on 18/1/26.
 */
public interface UserDbDAO {

    @Select("select * from sip_user_db where host=#{host}")
    Map<String, Object> queryDbSetting(@Param(value = "host") String host);


}
