package com.rp.sip.db.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * Created by cheungrp on 18/1/26.
 */
public interface SipTranDAO {

    @Select("select * from sip_tran_data where host=#{host}")
    List<Map<String, Object>> queryTran(@Param(value = "host") String host);

    @Select("select * from sip_tran_data where sip_server_id=#{serverId} and txCode=#{txCode}")
    Map<String, Object> queryTranByTxCode(@Param(value = "serverId") String serverId, @Param(value = "txCode") String txCode);

}
