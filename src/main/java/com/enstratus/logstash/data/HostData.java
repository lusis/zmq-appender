package com.enstratus.logstash.data;

import java.net.UnknownHostException;

public class HostData {

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String hostName;

    public HostData() {
        try {
            this.hostName = java.net.InetAddress.getLocalHost().getHostName();
        }catch (UnknownHostException e) {
            this.hostName = "unknown-host";
        }
    }
}
