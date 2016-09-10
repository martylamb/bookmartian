package com.martiansoftware.bookmartian.model;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mlamb
 */
public class JsonDirBookmarkCollection implements IBookmarkCollection {

    private static final Logger log = LoggerFactory.getLogger(JsonDirBookmarkCollection.class);
    
    private final JsonDirMap<Lurl, Bookmark> _map;    
    private final Object _lock = new Object();
    
    private JsonDirBookmarkCollection(Path dir) throws IOException {
        _map = JsonDirMap.newBuilder()
                        .path(dir)
                        .keyDesc("url")
                        .keyGetter(b -> ((Bookmark) b).lurl())
                        .valueClass(Bookmark.class)
                        .build();
        
        _map.values().forEach(b -> updateStructure(b));
    }
    
    public static JsonDirBookmarkCollection in(Path dir) throws IOException {
        return new JsonDirBookmarkCollection(dir);
    }

    private void updateStructure(Bookmark b) {
        try {
            if (!b.created().isPresent() || !b.modified().isPresent()) {
                Date now = new Date();
                Bookmark.Builder nb = b.toBuilder();
                if (!b.created().isPresent()) nb.created(now);
                if (!b.modified().isPresent()) nb.modified(now);
                add(nb.build());
            }
        } catch(IOException e) {
            log.error("Unable to update bookmark structure for {}: {}", b.lurl(), e.getMessage(), e);
        }
    }
    
    @Override
    public Bookmark get(Lurl lurl) {
        synchronized(_lock) {
            return _map.get(lurl);
        }
    }

    @Override
    public Bookmark remove(Bookmark b) throws IOException {
        synchronized(_lock) {
            return _map.removeByValue(b);
        }
    }
    
    @Override
    public Bookmark remove(Lurl lurl) throws IOException {
        synchronized(_lock) {
            return _map.remove(lurl);
        }
    }

    @Override
    public Bookmark add(Bookmark b) throws IOException {
        synchronized(_lock) {
            Bookmark original = get(b.lurl());
            Bookmark.Builder nb = b.toBuilder();
            Date now = new Date();
            nb.created(original == null ? now : original.created().orElse(now));
            nb.modified(now);
            Bookmark result = nb.build();
            _map.add(result);
            return result;
        }
    }

    @Override
    public Bookmark replace(Lurl replacing, Bookmark b) throws IOException {
        synchronized(_lock) {
            Bookmark result = add(b);
            if (!replacing.equals(b.lurl())) _map.remove(replacing);
            return result;
        }
    }

    @Override
    public List<Bookmark> all() {
        synchronized(_lock) {
            return Collections.unmodifiableList(_map.values().collect(Collectors.toList()));
        }
    }
    
}
