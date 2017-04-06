package com.martiansoftware.bookmartian.model;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author mlamb
 */
public interface ITagCollection {
    
    public List<Tag> all();
    public void add(Tag tag) throws IOException;
    public Tag remove(TagName tag) throws IOException;
    public Tag get(TagName name);
    public boolean contains(TagName name);
    
}
