package com.enstratus.logstash.layouts;

import com.enstratus.logstash.data.*;
import com.google.gson.*;

import java.net.UnknownHostException;
import java.util.*;
import org.apache.commons.lang.StringUtils;
import sun.net.idn.StringPrep;

public class JSONMessage {

    public String identity;
    public String[] tags;
    public LoggingEventData eventData;

    public LoggingEventData getEventData() {
        return eventData;
    }

    public void setEventData(LoggingEventData eventData) {
        this.eventData = eventData;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public JSONMessage() {

    }

    public JSONMessage(LoggingEventData event, String identity, String[] tags) {
        String localHost = identity;

        this.eventData = event;

        if (null == localHost) {
            localHost = new HostData().getHostName();
            // inject the identity
        }
        this.identity = localHost;

        if (!(null == tags)) {
            this.tags = tags;
        }

    }

    public String toJson() {
        String ident = this.getIdentity();
        String[] tagz = this.getTags();
        LoggingEventData event = this.getEventData();

        // Convert existing LoggingEventData to JsonObject
        // So we can inject some additional data
        JsonObject ed = (JsonObject) new Gson().toJsonTree(event);
        JsonParser parser = new JsonParser();
        ed.addProperty("identity", ident);

        if (!(null == tagz)) {
            JsonElement o = (JsonElement)parser.parse(new Gson().toJson(tags));
            ed.add("tags", o);
        }

        return new Gson().toJson(ed);
    }
}
