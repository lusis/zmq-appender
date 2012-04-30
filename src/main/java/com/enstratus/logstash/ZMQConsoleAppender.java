package com.enstratus.logstash;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.helpers.LogLog;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;
import org.apache.commons.lang.StringUtils;
import java.util.*;

public class ZMQConsoleAppender extends AppenderSkeleton {

    private Socket socket;

    // ZMQ specific "stuff"
    private int threads;
    private String listenAddress;

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public String getListenAddress() {
        return listenAddress;
    }

    public void setListenAddress(String listenAddress) {
        this.listenAddress = listenAddress;
    }

    public ZMQConsoleAppender() {
        super();
    }

    public ZMQConsoleAppender(final Socket socket) {
        this();
        this.socket = socket;
    }

    public void close() {

    }

    public boolean requiresLayout() {
        return true;
    }

    protected void append(LoggingEvent event) {
        List<String> topic = new ArrayList<String>();

        topic.add(event.getLevel().toString());
        topic.add(event.getLocationInformation().getClassName().toString());
        String formattedTopic = StringUtils.join(topic, '.');
        if (formattedTopic != null) {
            socket.send(formattedTopic.getBytes(), ZMQ.SNDMORE);
        }
        String logLine = layout.format(event);
        socket.send(logLine.getBytes(), ZMQ.NOBLOCK);
    }

    public void activateOptions() {
        super.activateOptions();

        final Context context = ZMQ.context(threads);
        Socket sender;

        sender = context.socket(ZMQ.PUB);
        sender.setLinger(1);

        final Socket socket = sender;

        socket.bind(listenAddress);
        this.socket = socket;
    }
}
