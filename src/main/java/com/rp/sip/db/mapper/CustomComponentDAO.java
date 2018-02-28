package com.rp.sip.db.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * Created by cheungrp on 18/1/26.
 */
public interface CustomComponentDAO {

    @Select("select * from sip_custom_component")
    Map<String, Object> queryDbCustom();


}
