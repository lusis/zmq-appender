package com.enstratus.logstash.data;

import sun.net.idn.StringPrep;

import java.util.*;
/**
 * Created with IntelliJ IDEA.
 * User: jvincent
 * Date: 4/28/12
 * Time: 10:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class TagList {
    private List<String> tags = new ArrayList<String>();

    public void addTag(String tag) {
        tags.add(tag);
    }
}
