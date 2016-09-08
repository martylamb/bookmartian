package com.martiansoftware.bookmartian.model;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author mlamb
 */
public interface IBookmartian {
    
    public List<Tag> tags();
    public Bookmark get(Lurl lurl);
    public List<Bookmark> bookmarksWithTags(TagNameSet query);
    public Bookmark replaceOrAdd(Lurl oldLurl, Bookmark toAdd) throws IOException;
    public Bookmark remove(Lurl lurl) throws IOException;

}
