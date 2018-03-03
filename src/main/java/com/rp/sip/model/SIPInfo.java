package com.rp.sip.model;

/**
 * Created by cheungrp on 18/3/3.
 */
public class SIPInfo {

    private String serverId;
    private String serverName;
    private String serverVersion;
    private String serverStat;

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    public String getServerStat() {
        return serverStat;
    }

    public void setServerStat(String serverStat) {
        this.serverStat = serverStat;
    }
}
