package com.enstratus.logstash;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.helpers.LogLog;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import com.google.gson.*;

import com.enstratus.logstash.data.LoggingEventData;
import com.enstratus.logstash.LogstashMessage;
import sun.net.idn.StringPrep;

public class ZMQAppender extends AppenderSkeleton {

	private Socket socket;
	private final Gson gson = new Gson();

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

	@Override
	public void close() {

	}

	@Override
	public boolean requiresLayout() {
        return false;
	}

	@Override
	protected void append(LoggingEvent event) {
		final LoggingEventData data = new LoggingEventData(event);
        String messageFormat = getEventFormat();
        String logLine = "";

        String identifier = getIdentity();
        String[] tagz;
        if(!(null == tags)) {
            tagz = getTags().split(",");
        }
        else
        {
            tagz = null;
        }

        if(JSONFORMAT.equals(messageFormat)) {
            JsonObject eventData = (JsonObject) gson.toJsonTree(data);
            JsonParser parser = new JsonParser();

            if (identifier != null) {
                String identity = "identity";
                eventData.addProperty(identity, identifier);
            }

            if (tags != null) {
                String tag_key = "tags";
                JsonElement o = (JsonElement)parser.parse(gson.toJson(tagz));
                LogLog.debug("tagz: " + gson.toJson(tagz));
                eventData.add(tag_key, o);
            }
            logLine = gson.toJson(eventData);
        }
        else if(JSONEVENTFORMAT.equals(messageFormat)) {
            LogstashMessage message = new LogstashMessage(data,identifier,tagz);
            logLine = gson.toJson(message);
        }
        if ((topic != null) && (PUBSUB.equals(socketType))) {
            socket.send(topic.getBytes(), ZMQ.SNDMORE);
        }

        socket.send(logLine.getBytes(), blocking ? 0 : ZMQ.NOBLOCK);
    }

	@Override
	public void activateOptions() {
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
			socket.setIdentity(identity.getBytes());
		}
		
		this.socket = socket;
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