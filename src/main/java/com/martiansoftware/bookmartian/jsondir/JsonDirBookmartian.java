package com.martiansoftware.bookmartian.jsondir;

import com.martiansoftware.bookmartian.model.Bookmark;
import com.martiansoftware.bookmartian.model.IBookmartian;
import com.martiansoftware.bookmartian.model.Lurl;
import com.martiansoftware.bookmartian.model.Query;
import com.martiansoftware.bookmartian.model.Tag;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private final AtomicBoolean _shutdown = new AtomicBoolean(false);
    private final String _name;
    
    private JsonDirBookmartian(Path dir, String username) throws IOException {
        _root = dir;
        _username = username;
        _name = "bookmartian for " + _username + " at " + _root.toAbsolutePath();
        log.info("starting {}", _name);
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
    
    private void checkShutdown() {
        synchronized(_lock) {
            if (_shutdown.get()) throw new IllegalStateException(_name + " has been shutdown.");
        }
    }
    
    @Override
    public Collection<Tag> tags() {
        synchronized(_lock) {
            checkShutdown();
            return _tags.all();
        }
    }
    
    @Override
    public Collection<Bookmark> bookmarks() {
        synchronized(_lock) {
            checkShutdown();
            return _bookmarks.all();
        }
    }
    
    @Override
    public Bookmark get(Lurl lurl) {
        synchronized(_lock) {
            checkShutdown();
            return _bookmarks.get(lurl);
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
            checkShutdown();
            return _bookmarks.replace(oldLurl, ensureTagsExistFor(toAdd));
        }
    }
    
    @Override
    public Bookmark visit(Lurl lurl) throws IOException {
        synchronized(_lock) {
            checkShutdown();
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
            checkShutdown();
            return _bookmarks.remove(lurl);
        }
    }    

    @Override
    public void shutdown() {
        log.info("shutting down {}", _name);
        synchronized(_lock) {
            _shutdown.set(true);
        }
        log.info("shut down {}", _name);
    }
}
