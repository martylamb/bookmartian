package com.martiansoftware.bookmartian;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.martiansoftware.util.Strings;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * collection of tag NAMES, not tags themselves
 * @author mlamb
 */
public class TagRefs {

    public static TagRefs EMPTY = new TagRefs();
    
    private final Set<String> _tags;
    
    public TagRefs(){
        _tags = Collections.unmodifiableSet(new java.util.TreeSet<>());
    }
    
    private TagRefs(Set<String> tags) {
        _tags = scrub(tags);
    }

    public TagRefs(String tags) {        
        _tags = scrub(Strings.splitOnWhitespaceAndCommas(tags));
    }
    
    private Set<String> scrub(Collection<String> tags) {
        return Collections.unmodifiableSet(
                (Set<String>) (
                tags == null
                ? new java.util.TreeSet<>()
                : tags.stream()
                    .map(s -> Strings.safeTrim(s))
                    .filter(s -> !s.isEmpty())
                    .map(s -> Tag.normalizeName(s))
                    .collect(Collectors.toCollection(java.util.TreeSet::new)))
        );
    }
    
    public Set<String> tagNames() {
        return _tags;
    }
    
    public boolean isEmpty() {
        return _tags.isEmpty();
    }
    
    public boolean contains(String tag) {
        return _tags.contains(Strings.safeTrim(tag).toLowerCase());
    }
    
    public boolean containsAll(TagRefs tagRefs) {
        for (String tag : tagRefs._tags) {
            if (!contains(tag)) return false;
        }
        return true;
    }
    
    public boolean containsAll(String tags) {
        return containsAll(new TagRefs(tags));
    }
    
    public static class GsonAdapter extends TypeAdapter<TagRefs> {        
        
        @Override
        public TagRefs read(JsonReader reader) throws IOException {            
            if (reader.peek() == JsonToken.NULL) return TagRefs.EMPTY;            
            reader.beginArray();
            Set<String> tags = new java.util.HashSet<>();
            while (reader.hasNext()) tags.add(reader.nextString());
            reader.endArray();
            return new TagRefs(tags);            
        }
        
        @Override
        public void write(JsonWriter writer, TagRefs value) throws IOException {
            if (value == null || value._tags.isEmpty()) {
                writer.nullValue();
                return;
            }
            writer.beginArray();
            for (String tag : value._tags) writer.value(tag);
            writer.endArray();
        }
    }
}
