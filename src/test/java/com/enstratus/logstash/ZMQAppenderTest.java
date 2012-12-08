package com.enstratus.logstash;

import java.util.*;

import net.minidev.json.parser.JSONParser;
import org.apache.log4j.*;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.jeromq.ZMQ;
import org.jeromq.ZMQException;
import org.junit.*;
import org.jeromq.ZMQ;
import org.jeromq.ZMQ.Context;
import org.jeromq.ZMQ.Socket;
import net.minidev.json.*;

import static org.junit.Assert.*;

public class ZMQAppenderTest {

    static ZMQAppender appender;
    static Logger logger;
	static Context context;
	static Socket receiver;
    static String zmqUrl;

    private static final String[] logstashFields = new String[] {
            "@message",
            "@source_host",
            "@fields",
            "@source_path",
            "@source",
            "@tags"
    };

    private static final String[] requiredAtFields = new String[] {
            "timestamp",
            "file",
            "class",
            "line_number",
            "method",
            "thread"
    };

	@BeforeClass
	public static void setup() {
        if(System.getProperty("zmqUrl") == null){
            zmqUrl = "tcp://127.0.0.1:20121";
        }else{
            zmqUrl = System.getProperty("zmqUrl");
        }
        logger = Logger.getRootLogger();
        appender = new ZMQAppender();
        appender.setEndpoint(zmqUrl);
        appender.setTags("test, case, tagz");
        appender.setIdentity("unit-tests");
        appender.setBlocking(true);
        appender.setThreads(1);
        appender.setSocketType("push");
        appender.setEventFormat("json_event");
        appender.setThreshold(Level.TRACE);
        appender.setMode("connect");
        appender.activateOptions();
        logger.addAppender(appender);


        context = ZMQ.context(2);
        receiver = context.socket(ZMQ.PULL);
        receiver.setLinger(1);
        receiver.bind(zmqUrl);

    }

    @AfterClass
    public static void cleanUp(){
        receiver.close();
        context.term();
    }

    @After
    public void clear(){
        NDC.clear();
        MDC.clear();
    }

	@Test
	public void doesntRequireLayout() {
		assertFalse("Appender should not require layout", appender.requiresLayout());
	}

    @Test(timeout = 500L)
    public void sendsValidJSONEvent(){
        MDC.put("test mdc", "data");
        NDC.push("ndc-value");
        logger.error("this is a test message", new IllegalArgumentException("shits on fire"));
        String message = getMessage();

        assertTrue("Event is not valid JSON", JSONValue.isValidJson(message));
    }

    @Test
    public void hasMDCData(){
        MDC.put("want","mdc");
        logger.warn("this is a warning with mdc");
        String message = getMessage();
        JSONObject jsonObject = (JSONObject) JSONValue.parse(message);
        JSONObject atFields = (JSONObject) jsonObject.get("@fields");
        JSONObject mdcData = (JSONObject) atFields.get("mdc");

        assertTrue("Missing mdc key", mdcData.containsKey("want"));
        assertEquals("Missing mdc data", "mdc", mdcData.get("want"));
    }

    @Test
    public void hadNDCData(){
        NDC.push("my-ndc");
        logger.warn("this is a warning with ndc");
        String message = getMessage();
        JSONObject jsonObject = (JSONObject) JSONValue.parse(message);
        JSONObject atFields = (JSONObject) jsonObject.get("@fields");
        String ndcData = atFields.get("ndc").toString();

        assertEquals("Missing ndc string","my-ndc",ndcData);
    }

    @Test
    public void hasExceptionInformation(){
        logger.fatal("this is a fatal with exception", new IllegalArgumentException("illegal argument dawg"));
        String message = getMessage();
        JSONObject jsonObject = (JSONObject) JSONValue.parse(message);
        JSONObject atFields = (JSONObject) jsonObject.get("@fields");

        assertTrue("Missing top-level exception key", atFields.containsKey("exception"));

        JSONObject exceptionInformation = (JSONObject) atFields.get("exception");

        List<String> exceptionKeys = Arrays.asList("exception_class", "exception_message", "stacktrace");

        for(String exceptionKey : exceptionKeys){
            assertTrue("Missing nested exception key: " + exceptionKey, exceptionInformation.containsKey(exceptionKey));
        }
        assertEquals("Incorrect exception class", exceptionInformation.get("exception_class"), "java.lang.IllegalArgumentException");
        assertEquals("Incorrect exception message", exceptionInformation.get("exception_message"), "illegal argument dawg");
        assertNotNull("Missing stack trace", exceptionInformation.get("stacktrace"));
    }

    @Test
    public void hasLogstashFields(){
        logger.info("this is an info message");
        String message = getMessage();
        JSONObject jsonObject = (JSONObject) JSONValue.parse(message);

        for(String keyName : logstashFields){
            assertTrue("Missing logstash field: "+keyName,jsonObject.containsKey(keyName));
            assertNotNull("Value for " + keyName + " cannot be null!",jsonObject.get(keyName));
        }
    }

    @Test
    public void hasAllRequiredFields(){
        logger.info("I need mah buckets");
        String message = getMessage();
        JSONObject jsonObject = (JSONObject) JSONValue.parse(message);
        JSONObject atFields = (JSONObject) jsonObject.get("@fields");

        for(String keyName : requiredAtFields){
            assertTrue("Missing @field key: "+ keyName, atFields.containsKey(keyName));
            assertNotNull("Value for " + keyName + " should not be null!", atFields.get(keyName));
        }
    }

    @Test
    public void hasClassName(){
        logger.debug("debug dawg");
        String message = getMessage();
        JSONObject jsonObject = (JSONObject) JSONValue.parse(message);
        JSONObject atFields = (JSONObject) jsonObject.get("@fields");

        assertTrue("Missing class key", atFields.containsKey("class"));
        assertNotNull("Class value should not be null", atFields.get("class"));
        assertEquals("Class name does not match", this.getClass().getCanonicalName().toString(), atFields.get("class"));
    }

    @Test
    public void hasFileName(){
        logger.debug("debug dawg");
        String message = getMessage();
        JSONObject jsonObject = (JSONObject) JSONValue.parse(message);
        JSONObject atFields = (JSONObject) jsonObject.get("@fields");

        assertTrue("Missing file key", atFields.containsKey("file"));
        assertNotNull("File value should not be null", atFields.get("file"));
        assertEquals("File name does not match", "ZMQAppenderTest.java", atFields.get("file"));
    }

    private String getMessage(){
        String message = receiver.recvStr(0);
        boolean more = receiver.hasReceiveMore();

        while(true){
            if(!more) { break;}
        }
        return message;
    }
}
