package com.martiansoftware.bookmartian.model;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author mlamb
 */
public class JsonDirBookmarkCollection implements IBookmarkCollection {

    private final JsonDirMap<Lurl, Bookmark> _map;    
    private final Object _lock = new Object();
    
    private JsonDirBookmarkCollection(Path dir) throws IOException {
        _map = JsonDirMap.newBuilder()
                        .path(dir)
                        .keyDesc("url")
                        .keyGetter(b -> ((Bookmark) b).lurl())
                        .valueClass(Bookmark.class)
                        .build();
    }
    
    public static JsonDirBookmarkCollection in(Path dir) throws IOException {
        return new JsonDirBookmarkCollection(dir);
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
            _map.add(b); // TODO: insert any bookmark stats here (e.g. "created", "modified", etc.)
            return b;
        }
    }

    @Override
    public Bookmark replace(Lurl replacing, Bookmark b) throws IOException {
        // todo: synchronize?
        synchronized(_lock) {
            _map.remove(replacing);
            _map.add(b); // TODO: insert any bookmark stats here (e.g. "created", "modified", etc.)
            return b;
        }
    }

    @Override
    public List<Bookmark> all() {
        synchronized(_lock) {
            return Collections.unmodifiableList(_map.values().collect(Collectors.toList()));
        }
    }
    
}
