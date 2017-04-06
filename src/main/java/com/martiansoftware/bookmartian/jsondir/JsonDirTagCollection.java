package com.martiansoftware.bookmartian.jsondir;

import com.martiansoftware.bookmartian.model.ITagCollection;
import com.martiansoftware.bookmartian.model.Tag;
import com.martiansoftware.bookmartian.model.TagName;
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
                        .valueClass(Tag.class)
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
    
    @Override
    public void add(Tag tag) throws IOException {
        synchronized(_lock) {
            _map.add(tag);
        }
    }
    
    @Override
    public Tag get(TagName name) { 
        synchronized(_lock) {
            return _map.get(name);
        }
    }
    
    @Override
    public boolean contains(TagName name) {
        return get(name) != null;
    }
    
    @Override
    public Tag remove(TagName name) throws IOException {
        synchronized(_lock) {
            return _map.remove(name);
        }
    }
}
