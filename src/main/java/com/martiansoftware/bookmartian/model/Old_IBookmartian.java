package com.martiansoftware.bookmartian.model;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 *
 * @author mlamb
 */
public interface Old_IBookmartian {
    
    public Collection<Tag> tags();
    public Collection<Bookmark> bookmarks();    
    public Bookmark get(Lurl lurl);
    public Bookmark replaceOrAdd(Lurl oldLurl, Bookmark toAdd) throws IOException;
    public Bookmark remove(Lurl lurl) throws IOException;
    public Bookmark visit(Lurl lurl) throws IOException;
    public void shutdown();
}
