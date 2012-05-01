package com.enstratus.logstash.layouts;

import com.enstratus.logstash.data.*;

import com.google.gson.*;
import java.util.*;
import org.apache.commons.lang.StringUtils;

public class LogstashMessage {

    private String source;
    private String source_host;
    private String source_path;
    private String file;
    private String message;

    private long timestamp;

    private String[] tags;
    private Map<String, Object> additionalFields = new HashMap<String, Object>();

    public LogstashMessage() {
    }

    public LogstashMessage(LoggingEventData event, String source_host, String[] tags) {
        String localHost = source_host;
        if (null == localHost) {
            localHost = new HostData().getHostName();
        }
        this.source_host = localHost;
        this.timestamp = event.time;
        this.file = event.info.file;
        this.source_path = event.log;
        if (!(null == event.thro)) {
            this.message = event.msg;
        }
        else
        {
            this.message = event.msg;
        }
        if (!(null == tags)) {
            this.tags = tags;
        }

        this.source = "file://"+localHost+"/"+event.info.file+"/"+event.info.clazz+"/"+event.info.method;

        // Populate the additionalFields
        this.additionalFields.put("fqn", event.fqn);
        this.additionalFields.put("class_file", event.info.clazz);
        this.additionalFields.put("level", event.level);
        this.additionalFields.put("line_number",event.info.line);
        this.additionalFields.put("thread", event.thread);
        if (!(null == event.thro)) {
            this.additionalFields.put("stacktrace", StringUtils.join(event.thro,"\n"));
        }
        if (!(null == event.mdc)){
            Map<String, String> mdc = event.mdc;
            for (Map.Entry<String, String> entry : mdc.entrySet())
            {
                this.additionalFields.put(entry.getKey(),entry.getValue());
            }
        }

    }

    public String toJson() {
        Map<String, Object> jm = new HashMap<String, Object>();

        jm.put("@source_host", source_host);
        jm.put("@source", source);
        jm.put("@timestamp", timestamp);
        jm.put("@source_path", source_path);
        jm.put("@message", message);
        jm.put("@tags", tags);
        jm.put("@fields",additionalFields);

        return new Gson().toJson(jm);
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSource_host() {
        return source_host;
    }

    public void setSource_host(String source_host) {
        this.source_host = source_host;
    }

    public String getSource_path() {
        return source_path;
    }

    public void setSource_path(String source_path) {
        this.source_path = source_path;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public Map<String, Object> getAdditionalFields() {
        return additionalFields;
    }

    public void setAdditionalFields(Map<String, Object> additionalFields) {
        this.additionalFields = additionalFields;
    }

}