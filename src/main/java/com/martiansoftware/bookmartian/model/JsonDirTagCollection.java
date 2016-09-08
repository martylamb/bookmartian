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
public class JsonDirTagCollection implements ITagCollection {
    
    private final JsonDirMap<TagName, Tag> _map;    
    private final Object _lock = new Object();
    
    private JsonDirTagCollection(Path dir) throws IOException {
        _map = JsonDirMap.newBuilder()
                        .path(dir)
                        .keyDesc("tag")
                        .keyGetter(t -> ((Tag) t).tagName())
                        .build();
    }
    
    public static JsonDirTagCollection in(Path dir) throws IOException {
        return new JsonDirTagCollection(dir);
    }

    @Override
    public List<Tag> all() {
        synchronized(_lock) {
            return Collections.unmodifiableList(_map.values().collect(Collectors.toList()));
        }
    }    
}
