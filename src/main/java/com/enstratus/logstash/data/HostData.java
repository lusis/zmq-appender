package com.enstratus.logstash.data;

import java.net.UnknownHostException;

public class HostData {

    private String hostName;

    public String getHostName() {
        return this.hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public HostData() {
        try {
            setHostName(java.net.InetAddress.getLocalHost().getHostName());
        }catch (UnknownHostException e) {
            setHostName("unknown-host");
        }
    }
}
