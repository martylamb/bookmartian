/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.martiansoftware.bookmartian.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
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
        _bookmarks = JsonDirBookmarkCollection.in(ensurePath("bookmarks"));
        _tags = JsonDirTagCollection.in(ensurePath("tags"));
        
        _bookmarks.all().stream().forEach(b -> ensureTagsExistFor(b));
    }
    
    private Path ensurePath(String path) throws IOException {
        return Files.createDirectories(_root.resolve("users").resolve(_username).resolve(path));
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
    public List<Bookmark> query(Set<String> queryTerms) {
        synchronized(_lock) {
            return Collections.unmodifiableList(
                    _bookmarks.all()
                    .stream()
                    .filter(Query.of(queryTerms))
                    .collect(Collectors.toList())
            );
        }
    }
  
    private Bookmark ensureTagsExistFor(Bookmark b) {
        b.tagNames()
            .asSet()
            .stream()
            .forEach(tn -> {
                if (!_tags.contains(tn)) {
                    try {
                        _tags.add(Tag.newBuilder().name(tn.toString()).build());
                    } catch (IOException e) {
                        log.error("unable to add tag [" + tn + "]: " + e.getMessage(), e);
                    }
                }
            });
        return b;
    }
    
    @Override
    public Bookmark replaceOrAdd(Lurl oldLurl, Bookmark toAdd) throws IOException {
        synchronized(_lock) {
            return _bookmarks.replace(oldLurl, ensureTagsExistFor(toAdd));
        }
    }
    
    @Override
    public Bookmark visit(Lurl lurl) throws IOException {
        synchronized(_lock) {
            Bookmark b = get(lurl);
            if (b == null) return null;
            return replaceOrAdd(null,
                                b.toBuilder()
                                    .lastVisited(new java.util.Date())
                                    .visitCount(b.visitCount().orElse(0l) + 1)
                                    .build());
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
