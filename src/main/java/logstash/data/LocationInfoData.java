package logstash.data;

import org.apache.log4j.spi.LocationInfo;

public class LocationInfoData {
	public String file;
	public String clazz;
	public String method;
	public String line;

	public LocationInfoData() {
		super();
	}

	public LocationInfoData(final LocationInfo info) {
		this();
		this.file = info.getFileName();
		this.clazz = info.getClassName();
		this.method = info.getMethodName();
		this.line = info.getLineNumber();
	}

	public LocationInfo toLocationInfo() {
		return new LocationInfo(file, clazz, method, line);
	}
}
