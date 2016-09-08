/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.martiansoftware.bookmartian.model;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mlamb
 */
public class JsonDirBookmartian implements IBookmartian {
    
    private static final Logger log = LoggerFactory.getLogger(JsonDirBookmartian.class);
    
    private final Path _root;
    private final JsonDirBookmarkCollection _bookmarks;
    private final JsonDirTagCollection _tags;
    private final String _username; // TODO: ensure only filesystem-safe usernames, or encode for filesystem
    private final Object _lock = new Object();
    
    private JsonDirBookmartian(Path dir, String username) throws IOException {
        _root = dir;
        _username = username;
        log.info("using data directory {}", _root.toAbsolutePath());
        _bookmarks = JsonDirBookmarkCollection.in(userPath("bookmarks"));
        _tags = JsonDirTagCollection.in(userPath("tags"));
    }
    
    private Path userPath(String path) {
        return _root.resolve("users").resolve(_username).resolve(path);
    }
    
    public static JsonDirBookmartian in(Path dir) throws IOException {
        return new JsonDirBookmartian(dir, "anonymous");
    }
    
    @Override
    public List<Tag> tags() {
        synchronized(_lock) {
            return _tags.all();
        }
    }
    
    @Override
    public Bookmark get(Lurl lurl) {
        synchronized(_lock) {
            return _bookmarks.get(lurl);
        }
    }
    
    @Override
    public List<Bookmark> bookmarksWithTags(TagNameSet query) {
        synchronized(_lock) {
            if (query.isEmpty()) return _bookmarks.all(); // already unmodifiable
            return Collections.unmodifiableList(
                        _bookmarks.all()
                            .stream()
                            .filter(b -> b.tagNames().containsAll(query))
                            .collect(Collectors.toList())
                    );
        }
    }
  
    @Override
    public Bookmark replaceOrAdd(Lurl oldLurl, Bookmark toAdd) throws IOException {
        synchronized(_lock) {
            // TODO: ensure tags exist!
            return _bookmarks.replace(oldLurl, toAdd);
        }
    }
    
    @Override
    public Bookmark remove(Lurl lurl) throws IOException {
        synchronized(_lock) {
            return _bookmarks.remove(lurl);
        }
    }    
    //    public Set<Bookmark> bookmarks(String query) {
//        TagNameSet qtags = new TagNameSet(query);
//        synchronized(_lock) {
//            if (qtags.isEmpty()) return bookmarks();           
//            return _bookmarks.stream()
//                    .filter(b -> b.tags().containsAll(qtags))
//                    .collect(Collectors.toCollection(java.util.TreeSet::new));
//        }
//    }

}
