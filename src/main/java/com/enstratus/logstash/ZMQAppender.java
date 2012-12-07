package com.enstratus.logstash;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.helpers.LogLog;
import org.jeromq.ZMQ;
import org.jeromq.ZMQ.Context;
import org.jeromq.ZMQ.Socket;

import com.enstratus.logstash.data.LoggingEventData;
import com.enstratus.logstash.layouts.*;

public class ZMQAppender extends AppenderSkeleton {

	private Socket socket;

	// ZMQ specific "stuff"
	private int threads;
	private int hwm;
	private String endpoint;
	private String mode;
	private String socketType;
	private String topic;
	private String identity;
	private boolean blocking;
	
	// Ancillary settings
	private String tags;
	private String eventFormat = "json_event";
	
	private static final String PUBSUB = "pub";
	private static final String PUSHPULL = "push";
	private static final String CONNECTMODE = "connect";
	private static final String BINDMODE = "bind";
    private static final String JSONFORMAT = "json";
    private static final String JSONEVENTFORMAT = "json_event";

	public ZMQAppender() {
		super();
	}

	public ZMQAppender(final Socket socket) {
		this();
		this.socket = socket;
	}

	public void close() {

	}

	public boolean requiresLayout() {
        return false;
	}

	@Override
	protected void append(LoggingEvent event) {
        LogLog.debug("Got append event");
		final LoggingEventData data = new LoggingEventData(event);
        String messageFormat = getEventFormat();
        LogLog.debug("Message format: "+ messageFormat);
        String logLine = "";

        String identifier = getIdentity();
        String[] tagz;
        if(tags != null) {
            LogLog.debug("Tags: " + tags);
            tagz = getTags().split(",");
        }
        else
        {
            tagz = null;
        }

        if(JSONFORMAT.equals(messageFormat)) {
            JSONMessage message = new JSONMessage(data,identifier,tagz);
            logLine = message.toJson();
        }
        else if(JSONEVENTFORMAT.equals(messageFormat)) {
            LogstashMessage message = new LogstashMessage(data,identifier,tagz);
            logLine = message.toJson();
        }
        if ((topic != null) && (PUBSUB.equals(socketType))) {
            LogLog.debug("Sending topic: "+ topic.getBytes());
            socket.send(topic.getBytes(), ZMQ.SNDMORE);
        }
        LogLog.debug("Blocking? " + blocking);
        socket.send(logLine.getBytes(), blocking ? 0 : ZMQ.NOBLOCK);
    }

	@Override
	public void activateOptions() {
        LogLog.debug("Configuring appender...");
		super.activateOptions();

		final Context context = ZMQ.context(threads);
		Socket sender;

		if (PUBSUB.equals(socketType)) {
			LogLog.debug("Setting socket type to PUB");
			sender = context.socket(ZMQ.PUB);
		}
		else if (PUSHPULL.equals(socketType))
		{
			LogLog.debug("Setting socket type to PUSH");
			sender = context.socket(ZMQ.PUSH);
		}
		else
		{
			LogLog.debug("Setting socket type to default PUB");
			sender = context.socket(ZMQ.PUB);
		}
		sender.setLinger(1);
		
		final Socket socket = sender;
		
		final String[] endpoints = endpoint.split(",");
		
		for(String ep : endpoints) {
			
			if (BINDMODE.equals(mode)) {
				LogLog.debug("Binding socket to " + ep);
				socket.bind(ep);
			}
			else if (CONNECTMODE.equals(mode))
			{
				LogLog.debug("Connecting socket to " + ep);
				socket.connect(ep);
			}
			else
			{
				LogLog.debug("Default connecting socket to " + ep);
				socket.connect(ep);
			}	
		}
		
		if (identity != null) {
            LogLog.debug("Setting identity to: " + identity);
			socket.setIdentity(identity.getBytes());
		}
		
		this.socket = socket;
        LogLog.debug("Finished configuring appender");
	}

	public int getThreads() {
		return threads;
	}

	public void setThreads(final int threads) {
		this.threads = threads;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(final String endpoint) {
		this.endpoint = endpoint;
	}

	public boolean isBlocking() {
		return blocking;
	}

	public void setBlocking(boolean blocking) {
		this.blocking = blocking;
	}
	
	public String getTopic() {
		return topic;
	}
	
	public void setTopic(final String topic) {
		this.topic = topic;
	}
	
	public String getSocketType() {
		return socketType;
	}
	
	public void setSocketType(final String socketType) {
		this.socketType = socketType;
	}
	
	public String getMode() {
		return mode;
	}
	
	public void setMode(final String mode) {
		this.mode = mode;
	}
	
	public String getIdentity() {
		return identity;
	}
	
	public void setIdentity(final String identity) {
		this.identity = identity;
	}
	
	public String getTags() {
		return tags;
	}
	
	public void setTags(final String tags) {
		this.tags = tags;
	}
	
	public String getEventFormat() {
		return eventFormat;
	}

	public void setEventFormat(final String eventFormat) {
		this.eventFormat = eventFormat;
	}
}