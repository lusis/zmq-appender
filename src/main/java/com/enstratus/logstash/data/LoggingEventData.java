package com.enstratus.logstash.data;

import java.util.Map;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

public class LoggingEventData {
	public String fqn;
	public String log;
	public long time;
	public String level;
	public String msg;
	public String thread;
	public String ndc;
	public LocationInfoData info;
    public HashMap<String, Object> exceptionInformation;
	@SuppressWarnings("rawtypes")
	public Map mdc;

	public LoggingEventData() {
		super();
	}

	public LoggingEventData(final LoggingEvent event) {
        this();
		this.log = event.getLoggerName();
		this.time = event.getTimeStamp();
		this.level = event.getLevel().toString();
		this.msg = event.getMessage().toString();
		this.thread = event.getThreadName();
		final ThrowableInformation throwableInformation = event.getThrowableInformation();
        HashMap<String, Object> ex = new HashMap<String, Object>();
		if (throwableInformation != null) {
            if(throwableInformation.getThrowable().getClass().getCanonicalName() != null){
                ex.put("exception_class",throwableInformation.getThrowable().getClass().getCanonicalName());
            }
            if(throwableInformation.getThrowable().getMessage() != null) {
                ex.put("exception_message",throwableInformation.getThrowable().getMessage());
            }
            if( throwableInformation.getThrowableStrRep() != null) {
                String stackTrace = StringUtils.join(throwableInformation.getThrowableStrRep(),"\n");
                ex.put("stacktrace",stackTrace);
            }
		}
        this.exceptionInformation = ex;
		this.ndc = event.getNDC();
		this.info = new LocationInfoData(event.getLocationInformation());
		this.mdc = event.getProperties();
	}
}
