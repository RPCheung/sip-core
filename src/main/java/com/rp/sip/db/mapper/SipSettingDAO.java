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

    @Select("select * from sip_setting ")
    Map<String, Object> querySetting();


    @Update("update sip_setting  set "
            + "port = #{port}, "
            + "charset = #{charset},"
            + "txCodePath = #{txCodePath},"
            + "initialBytesToStrip = #{initialBytesToStrip},"
            + "lengthAdjustment = #{lengthAdjustment},"
            + "msgType = #{msgType},"
            + "IONum = #{IONum},"
            + "lengthIncludesLengthFieldLength = #{lengthIncludesLengthFieldLength},"
            + "timeout = #{timeout},"
            + "lengthFieldOffset = #{lengthFieldOffset},"
            + "lengthFieldLength = #{lengthFieldLength},"
            + "connectNum = #{connectNum},"
            + "httpContext = #{httpContext},"
            + "protocol = #{protocol},"
            + "txCodePojo = #{txCodePojo},"
            + "executorPoolSize = #{executorPoolSize}"
            + " where host = #{host}")
    boolean editSetting(Map<String, Object> parameters);


}
