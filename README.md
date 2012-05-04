# ZeroMQ log4j appender

CURRENT RELEASE: 0.4.0 for use with Logstash Master branch

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
- Tags
- Different formats (`json` vs. `json_event`)

We're running this @enStratus pushing 600k events an hour (INFO logging for all applications) and have yet to have a problem with it.

# Sample properties

```
log4j.rootCategory=TRACE,Zmq


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
# Valid messageFormat options are json and json_event
# default is json_event
# When using pub sockets,
# you can also set a topic
# log4j.appender.Zmq.topic=footopic
# 
# If you want to add any tags:
log4j.appender.Zmq.tags=foo,bar,baz
```

## `json` format output
This is the original format shipped by Logcentric. I don't use it anymore but I have no intention of getting rid of it.

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
  "identity":"appender-test",
  "tags":["foo","bar","baz"]
}
```

## `json_event` format output

The default format is `json_event`. This is designed to lay the message out in a format that requires less filtering on the part of logstash. In fact, you can pass through these messages with no filters whatsoever and they'll do the right thing. In fact, if you leverage tags appropriately at the appender side, you can easily do routing just on tags alone.

Note that `tags` may or may not be present. Also note that the `stacktrace` may or may not be present.

```json
{
    "@source": "file: //appender-test/Log4jExample.java/foo.Log4jExample/main",
    "@source_host": "appender-test",
    "@source_path": "foo.Log4jExample",
    "@file": "Log4jExample.java",
    "@message": "Hello this is an fatal message",
    "@tags": [
        "zmq",
        "foo",
        "bar",
        "baz"
    ],
    "additionalFields": {
    	"timestamp": 1335760642237,
        "class_file": "foo.Log4jExample",
        "fqn": "org.apache.log4j.Category",
        "level": "FATAL",
        "thread": "main",
        "line_number": "20",
        "stacktrace": "java.io.IOException: Ithrewanexception\\n\\tatfoo.Log4jExample.main(Log4jExample.java: 20)"
    }
}
```

### Timestamp notes
One thing to note here is that the timestamp of the event lives in `additionalFields`. The reason for this is in how log4j reports timestamps. Rather than fight with building something to convert the java epoch w/millisecond timestamps to ISO8601 (or pull in another dep that could cause issues), I chose to move the event timestamp to an additional field. If you would like to re-timestamp the event, use the `date` filter in logstash to do so converting this field.

### Type field
Also note that no `@type` field is being sent. This is to reduce confusion on the logstash side. Inputs in logstash REQUIRE a type but if the appender were to set the type, it would OVERWRITE the type set in the input stanza of the Logstash configuration. This could cause some serious WTF moments when trying to route to specific outputs.

_As of this release (0.4.0) the fixes for pure `json_events` in logstash inputs ONLY exist in Logstash master. I will update this README when the next release of logstash happens._

## About identity
If you set the `identity` property, this will not only set the socket identity but also add a new field to the JSON named based on the `eventFormat` property: 

- `json_event` will call the field `source_host`
- `json` will call the field `identity`

The reason for this is that there is currently NO way in ZeroMQ to get a peer's identity outside of making it part of the message.
If you do not set an identity, when using `json_event`, the identity will be set to the local machine name. In the event that fails, the identity will be `unknown-host`.


## Sample logstash input for the above example

This is a sample logstash input. I'm currently using something akin to this in production:

```
input {
  zeromq {
    type => "zmq-input"
    topology => "pushpull"
    address => "tcp://*:5556"
    mode => "server"
    format => "json_event" # You should use json regardless of the eventFormat property (for now)
  }
}

filter {
  # set the timestamp of the message
  # from the `time` value
  # for json eventFormat
  date { time => "UNIX_MS" }
  # for json_event eventFormat
  date { timstamp => "UNIX_MS" }
 
  # Not all of these are neccessary when using
  # eventFormat of json_event
  # shift things around
  # to make sense
  mutate {
    remove => ["time", "fqn", "info", "mdc"]
    replace => ["@message", "%{msg}"] # change msg to message for json_event
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

# Credits
I've not done much real work myself. This has been done by muddling through the existing work of the logcentric code and figuring out Java in the process.

Both [@flangy](http://twitter.com/flangy) and [@jostheim](http://twitter.com/jostheim) have been awesomely helpful in this process.

All the hard work was done by the author of logcentric.

Additionally, this is being done as part of my work for enStratus on building out our logging infrastructure.

# License

Apache 2.0
