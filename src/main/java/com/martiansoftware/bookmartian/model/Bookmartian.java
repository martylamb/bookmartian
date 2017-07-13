package com.martiansoftware.bookmartian.model;

import java.util.Collection;
import java.util.Optional;

/**
 *
 * @author mlamb
 */
public interface Bookmartian {
   
    public Collection<Bookmark> bookmarks(); // all bookmarks
    // getBookmark?
    public Optional<Bookmark> get(Lurl lurl); // lookup one bookmark by url
    // removeBookmark?
    public Optional<Bookmark> remove(Lurl lurl); // remove bookmark by url, return the one that was deleted (if any)
    // visitBookmark?
    public Optional<Bookmark> visit(Lurl lurl); // updates visit count and visit timestamp and returns updated bookmark, or returns Optional.empty if no such bookmark exists.
    // updateBookmark?
    public Bookmark update(Lurl replacing, Bookmark bookmark); // saves the bookmark, potentially replacing a bookmark at a different url
    
    public Collection<Tag> tags(); // all tags
    public Optional<Tag> get(TagName tn);
    
    public void shutdown();
}
