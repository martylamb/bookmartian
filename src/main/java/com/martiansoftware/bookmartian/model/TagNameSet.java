package com.martiansoftware.bookmartian.model;

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
 * collection of TagNames - not Tags themselves
 * @author mlamb
 */
public class TagNameSet {

    public static final TagNameSet EMPTY = new TagNameSet();
    
    private final Set<TagName> _tagNames;
    
    private TagNameSet(){
        this(new java.util.TreeSet<>());
    }
    
    private TagNameSet(Set<TagName> tagNames) {
        _tagNames = Collections.unmodifiableSet(tagNames);
    }
    
    public static TagNameSet of(Set<TagName> tagNames) {
        return new TagNameSet(new java.util.TreeSet<>(tagNames));
    }
    
    public static TagNameSet of(String multipleTags) {
        return new TagNameSet(scrub(Strings.splitOnWhitespaceAndCommas(multipleTags)));
    }
    
    public static TagNameSet of(Collection<String> tagNames) {
        return new TagNameSet(scrub(tagNames));
    }
        
    private static Set<TagName> scrub(Collection<String> tagNames) {
        return (Set<TagName>) (
                tagNames == null
                ? EMPTY
                : tagNames.stream()
                    .map(s -> TagName.of(s))
                    .collect(Collectors.toCollection(java.util.TreeSet::new))
        );
    }
    
    public Set<TagName> asSet() {
        return _tagNames;
    }
    
    public boolean isEmpty() {
        return _tagNames.isEmpty();
    }
    
    public boolean contains(TagName tagName) {
        return _tagNames.contains(tagName);
    }
    
    public boolean containsAll(TagNameSet tagRefs) {
        for (TagName tagName : tagRefs._tagNames) {
            if (!contains(tagName)) return false;
        }
        return true;
    }
    
    public boolean containsAll(String multipleTags) {
        return containsAll(TagNameSet.of(multipleTags));
    }
    
    @Override
    public String toString() {
        return _tagNames.stream().map(tn -> tn.toString()).collect(Collectors.joining(" "));            
    }
    
    public static class GsonAdapter extends TypeAdapter<TagNameSet> {        
        
        @Override
        public TagNameSet read(JsonReader reader) throws IOException {            
            if (reader.peek() == JsonToken.NULL) return TagNameSet.EMPTY;            
            reader.beginArray();
            Set<String> tags = new java.util.HashSet<>();
            while (reader.hasNext()) tags.add(reader.nextString());
            reader.endArray();
            return TagNameSet.of(tags);            
        }
        
        @Override
        public void write(JsonWriter writer, TagNameSet value) throws IOException {
            if (value == null || value._tagNames.isEmpty()) {
                writer.nullValue();
                return;
            }
            writer.beginArray();
            for (TagName t : value._tagNames) writer.value(t.toString());
            writer.endArray();
        }
    }
}
