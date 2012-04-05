package com.enstratus.logstash;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import com.google.gson.Gson;
import com.enstratus.logstash.data.LoggingEventData;

public class ZMQAppender extends AppenderSkeleton implements Appender {

	private Socket socket;
	private final Gson gson = new Gson();

	private int threads;
	private String endpoint;
	private String mode;
	private String socketType;
	private String topic;
	private String identity;
	private boolean blocking;
	
	private static final String PUBSUB = "pub";
	private static final String PUSHPULL = "push";
	private static final String CONNECTMODE = "connect";
	private static final String BINDMODE = "bind";

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

	/**
	 * ZMQAppender doesn't require layout, just publishes log events over
	 * ZeroMQ. Real logging tasks are up to subscribers.
	 */
	@Override
	public boolean requiresLayout() {
		return false;
	}

	@Override
	protected void append(final LoggingEvent event) {
		final LoggingEventData data = new LoggingEventData(event);
		final String json = gson.toJson(data);
		if ((topic != null) && (PUBSUB.equals(socketType))) {
			socket.send(topic.getBytes(), ZMQ.SNDMORE);
		}
		socket.send(json.getBytes(), blocking ? 0 : ZMQ.NOBLOCK);
	}

	@Override
	public void activateOptions() {
		super.activateOptions();

		final Context context = ZMQ.context(threads);
		Socket sender;
		if (PUBSUB.equals(socketType)) {
			sender = context.socket(ZMQ.PUB);
		}
		else if (PUSHPULL.equals(socketType))
		{
			sender = context.socket(ZMQ.PUSH);
		}
		else
		{
			sender = context.socket(ZMQ.PUB);
		}
		
		final Socket socket = sender;
		
		if (BINDMODE.equals(mode)) {
			socket.bind(endpoint);
		}
		else if (CONNECTMODE.equals(mode))
		{
			socket.connect(endpoint);
		}
		else
		{
			socket.connect(endpoint);
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
}
