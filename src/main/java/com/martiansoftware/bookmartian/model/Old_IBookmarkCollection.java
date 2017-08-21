package com.martiansoftware.bookmartian.model;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author mlamb
 */
public interface Old_IBookmarkCollection {
    
    public Bookmark get(Lurl lurl);
    public Bookmark remove(Bookmark b) throws IOException;
    public Bookmark remove(Lurl lurl) throws IOException;
    public Bookmark add(Bookmark b) throws IOException;
    public Bookmark replace(Lurl replacing, Bookmark b) throws IOException;
    public List<Bookmark> all();
}
