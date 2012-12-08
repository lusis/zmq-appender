package com.enstratus.logstash.data;

import java.util.*;

public class TagData {
    private List<String> tags = new ArrayList<String>();

    public void addTag(String tag) {
        tags.add(tag);
    }
}
