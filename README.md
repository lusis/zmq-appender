# ZeroMQ log4j appender

I had a need for a log4j appender that used ZMQ for transport. The only place that existed was here:

https://github.com/ichiban/logcentric

It met most of my needs (mainly that all the hard work was done) however there were a few problems:

- It worked in bind mode only
- It only supported pubsub socket pairs
- Support for topics
- It had a bunch of other stuff with it
- I can't write Java

I knew the Java ecosystem well enough. I can navigate the code and follow logic and what not. However I don't know the language from a creation side. So I figured, like with every other thing, that I'd learn a little by scratching an itch.

# What works
Without unit tests (something I need to learn to do next) I cannot say that everything works as intended.

However in my testing the expected functionality indeed works:

- pushpull socket pair
- pusbsub socket pair
- Topics
- connect vs. bind
- Load balanced sockets

I've tested sending from a real application to a logstash instance listening on the appropriate socket pair type. I've also tested with a ghetto sender app that I cribbed from the log4j website.

# Sample properties

```
# The Root uses the Console
log4j.rootCategory=TRACE,Console,Zmq


# Setup the Console
log4j.appender.Console=org.apache.log4j.ConsoleAppender
log4j.appender.Console.Threshold=TRACE
log4j.appender.Console.layout=org.apache.log4j.PatternLayout
log4j.appender.Console.layout.ConversionPattern=[%d{dd MMM yyyy HH:mm:ss.SSS}] [%p.%c] %m%n

log4j.appender.Zmq=com.enstratus.logstash.ZMQAppender
log4j.appender.Zmq.Threshold=TRACE
# If you need to load balance multiple peers,
# you can specify multiple endpoints
# separated by a comma i.e.
# tcp://127.0.0.1:5556,tcp://127.0.0.1:5557
log4j.appender.Zmq.endpoint=tcp://127.0.0.1:5556
log4j.appender.Zmq.blocking=true
log4j.appender.Zmq.threads=1
# Valid socketType options are push and pub
log4j.appender.Zmq.socketType=push
log4j.appender.Zmq.identity=appender-test
# When using pub sockets,
# you can also set a topic
# log4j.appender.Zmq.topic=footopic
```

## JSON output

```json
{
  "fqn":"org.apache.log4j.Category",
  "log":"foo.Log4jExample",
  "time":1333855070562,
  "level":"WARN",
  "msg":"Hello this is a warn message",
  "thread":"main",
  "info":{
    "file":"Log4jExample.java",
    "clazz":"foo.Log4jExample",
    "method":"main",
    "line":"16"
  },
  "mdc":{},
  "identity":"appender-test"
}
```

If you set the `identity` property, this will not only set the socket identity but also add a new field to the JSON called, surprisingly, identity. The reason for this is that there is currently NO way in ZeroMQ to get a peer's identity outside of making it part of the message.

## Sample logstash input for the above example

This is a sample logstash input. I'm currently using something akin to this in production:

```
input {
  zeromq {
    type => "zmq-input"
    topology => "pushpull"
    address => "tcp://*:5556"
    mode => "server"
    format => "json"
  }
}

filter {
  # set the timestamp of the message
  # from the `time` value
  date { time => "UNIX_MS" }
  
  # shift things around
  # to make sense
  mutate {
    remove => ["time", "fqn", "info", "mdc"]
    replace => ["@message", "%{msg}"]
    replace => ["@source_host", "%{identity}"]
    replace => ["@source_path", "%{thread}"]
  }
  
  # remove msg as we've shifted
  # its contents to @message
  mutate {
    remove => ["msg"]
  }
}

output {
	#stdout { debug => true debug_format => "json" }
	elasticsearch { embedded => true }
}
```

# TODO

- I still need to expose more sockopts - HWM, BACKLOG
- I should probably make PAIR socket types an option as well
- Probably going to end up converting to a custom layout for logevent
- Add more information to the JSON?

# Credits
I've not done much real work myself. This has been done by muddling through the existing work of the logcentric code and figuring out Java in the process.

Both [@flangy](http://twitter.com/flangy) and [@jostheim](http://twitter.com/jostheim) have been awesomely helpful in this process.

All the hard work was done by the author of logcentric.

Additionally, this is being done as part of my work for enStratus on building out our logging infrastructure.

# License

Apache 2.0
