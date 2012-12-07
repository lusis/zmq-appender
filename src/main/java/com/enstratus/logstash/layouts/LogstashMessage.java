package com.enstratus.logstash.layouts;

import com.enstratus.logstash.data.*;

import net.minidev.json.JSONObject;
import java.util.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.spi.ThrowableInformation;
import org.apache.log4j.helpers.LogLog;

public class LogstashMessage {

    private String source;
    private String source_host;
    private String source_path;
    private String file;
    private String message;

    private long timestamp;

    private String[] tags = null;
    private String myNdc = null;
    private Map<String, Object> myMdc = new HashMap<String, Object>();
    private Map<String, Object> additionalFields = new HashMap<String, Object>();
    private Map<String, Object> myExceptionInformation = new HashMap<String, Object>();

    public LogstashMessage(LoggingEventData event, String source_host, String[] tags) {
        LogLog.debug("Entering LogstashMessage");

        String localHost = source_host;
        if (localHost == null) {
            localHost = new HostData().getHostName();
        }
        setSource_host(localHost);
        setTimestamp(event.time);
        setFile(event.info.file);
        setSource_path(event.log);
        setMessage(event.msg);

        if (tags != null) {
            setTags(tags);
        }

        setSource("file://"+localHost+"/"+event.info.file+"/"+event.info.clazz+"/"+event.info.method);

        // Populate the additionalFields
        additionalFields.put("method", event.info.method);
        additionalFields.put("timestamp", getTimestamp());
        additionalFields.put("class", event.info.clazz);
        additionalFields.put("file", event.info.file);
        additionalFields.put("level", event.level);
        additionalFields.put("line_number",event.info.line);
        additionalFields.put("thread", event.thread);

        Iterator<Map.Entry<String, Object>> exInfo = event.exceptionInformation.entrySet().iterator();
        while(exInfo.hasNext()){
            Map.Entry<String,Object> entry = exInfo.next();
            myExceptionInformation.put(entry.getKey(),entry.getValue());
        }

        if (event.mdc != null){
            myMdc = event.mdc;
        }
        if (event.ndc != null) {
            myNdc = event.ndc;
        }
        additionalFields.put("ndc",myNdc);
        additionalFields.put("mdc",myMdc);
        additionalFields.put("exception", myExceptionInformation);

    }

    public String toJson() {
        JSONObject logstashEvent = new JSONObject();

        logstashEvent.put("@source_host", source_host);
        logstashEvent.put("@source", source);
        logstashEvent.put("@source_path", source_path);
        logstashEvent.put("@message", message);
        logstashEvent.put("@tags", tags);
        logstashEvent.put("@fields",additionalFields);

        return logstashEvent.toJSONString();
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