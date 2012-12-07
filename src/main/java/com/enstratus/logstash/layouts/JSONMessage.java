package com.enstratus.logstash.layouts;

import com.enstratus.logstash.data.*;
import net.minidev.json.JSONObject;


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

    public JSONMessage(LoggingEventData event, String identity, String[] tags) {
        String localHost = identity;

        setEventData(event);

        if (null == localHost) {
            localHost = new HostData().getHostName();
        }

        setIdentity(localHost);

        if (!(null == tags)) {
            setTags(tags);
        }

    }

    public String toJson() {
        String ident = this.getIdentity();
        String[] tagz = this.getTags();
        LoggingEventData event = this.getEventData();
        JSONObject jsonEvent = new JSONObject();

        // Convert existing LoggingEventData to JsonObject
        // So we can inject some additional data

        jsonEvent.put("fqn", event.fqn);
        jsonEvent.put("identity", ident);

        if (!(null == tagz)) {
            jsonEvent.put("tags",tagz);
        }

        return jsonEvent.toJSONString();
    }
}
