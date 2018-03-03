package com.rp.sip.db.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * Created by cheungrp on 18/1/26.
 */
public interface SipSettingDAO {

    @Select("select * from sip_setting where sip_server_id=#{serverId}")
    Map<String, Object> querySetting(@Param(value = "serverId") String serverId);

}
