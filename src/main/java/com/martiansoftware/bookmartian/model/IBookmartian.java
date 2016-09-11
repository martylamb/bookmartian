package com.martiansoftware.bookmartian.model;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 *
 * @author mlamb
 */
public interface IBookmartian {
    
    public List<Tag> tags();
    public Bookmark get(Lurl lurl);
    public List<Bookmark> query(Set<String> queryTerms);
    public Bookmark replaceOrAdd(Lurl oldLurl, Bookmark toAdd) throws IOException;
    public Bookmark remove(Lurl lurl) throws IOException;
    public Bookmark visit(Lurl lurl) throws IOException;
}
