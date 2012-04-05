package com.enstratus.logstash;

import static org.easymock.EasyMock.aryEq;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.junit.Before;
import org.junit.Test;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import com.google.gson.Gson;
import com.enstratus.logstash.data.LoggingEventData;

public class ZMQAppenderTest {
	Gson gson = new Gson();

	// unit
	ZMQAppender appender;

	Socket socket;

	List<LoggingEvent> events = Arrays.asList(event(Level.FATAL, "fatal"),
			event(Level.ERROR, "error"), event(Level.INFO, "info"),
			event(Level.TRACE, "trace"), event(Level.DEBUG, "debug"));

	LoggingEvent event(Level level, Object message) {
		Logger logger = Logger.getLogger(ZMQAppenderTest.class);
		String fqn = "com.y1ban.zmq_appender.test";
		long timeStamp = Calendar.getInstance().getTimeInMillis();
		String threadName = "testThread";
		Throwable throwable = new NullPointerException();
		String ndc = "ndc";
		String file = "";
		String classname = "";
		String method = "";
		String line = "";
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("thisis", "test");

		return new LoggingEvent(fqn, logger, timeStamp, level, message,
				threadName, new ThrowableInformation(throwable), ndc,
				new LocationInfo(file, classname, method, line), properties);
	}

	@Before
	public void setup() {
		socket = createMock(Socket.class);
		appender = new ZMQAppender(socket);
	}

	@Test
	public void doesntRequireLayout() {
		assertFalse(appender.requiresLayout());
	}

	@Test
	public void appendsLoggingEvents() {
		for (LoggingEvent event : events) {
			final LoggingEventData data = new LoggingEventData(event);
			expect(
					socket.send(aryEq(gson.toJson(data).getBytes()),
							eq(ZMQ.NOBLOCK))).andReturn(true);
		}
		replay(socket);

		for (LoggingEvent event : events) {
			appender.append(event);
		}

		verify(socket);
	}
}
