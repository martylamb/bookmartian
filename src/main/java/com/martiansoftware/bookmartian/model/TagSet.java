package com.martiansoftware.bookmartian.model;

import java.util.Collection;
import java.util.Map;

/**
 *
 * @author mlamb
 */
public class TagSet {

    private final Map<TagName, Tag> _tags = new java.util.TreeMap<>();
    
    public TagSet(){}
    
    public TagSet add(Tag t) {
        _tags.put(t.tagName(), t);
        return this;
    }
    
    public TagSet remove(Tag t) {
        _tags.remove(t.tagName());
        return this;
    }
    
    public int size() {
        return _tags.size();
    }
}
