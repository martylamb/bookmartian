package com.martiansoftware.bookmartian.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.martiansoftware.util.Strings;
import com.martiansoftware.validation.Hope;
import java.lang.reflect.Type;
import java.text.Collator;
import java.util.Comparator;
import java.util.Date;
import java.util.Optional;

/**
 *
 * @author mlamb
 */
public class Bookmark {
    private final Lurl _url;
    private final Optional<String> _title, _notes, _imageUrl;
    private final TagNameSet _tags;
    private final Optional<Date> _created, _modified, _lastVisited;
    private final Optional<Long> _visitCount;
    
    private static final Collator COLLATOR = Collator.getInstance();
    static {
        COLLATOR.setStrength(Collator.SECONDARY);
    }
    
    public static final Comparator<Bookmark> MOST_VISITED_FIRST = (a, b) -> Long.compare(b.cmpVisitCount(), a.cmpVisitCount());    
    public static final Comparator<Bookmark> MOST_RECENTLY_CREATED_FIRST = (a, b) -> Long.compare(b.cmpCreated(), a.cmpCreated());
    public static final Comparator<Bookmark> MOST_RECENTLY_VISITED_FIRST = (a, b) -> Long.compare(b.cmpLastVisited(), a.cmpLastVisited());
    public static final Comparator<Bookmark> MOST_RECENTLY_MODIFIED_FIRST = (a, b) -> Long.compare(b.cmpModified(), a.cmpModified());   
    public static final Comparator<Bookmark> BY_TITLE = (a, b) -> COLLATOR.compare(a.title().orElse(""), b.title().orElse(""));
    public static final Comparator<Bookmark> BY_URL = (a, b) -> COLLATOR.compare(a.lurl().toString(), b.lurl().toString());
    public static Comparator<Bookmark> BY_TAGNAME(TagName tn) {
        return (a, b) -> {
            long av = (a.tagNames().contains(tn)) ? 0 : 1;
            long bv = (b.tagNames().contains(tn)) ? 0 : 1;
            return Long.compare(av, bv);
        };                
    }
    
    private Bookmark(Lurl lurl,
                        String title, 
                        String notes, 
                        String imageUrl, 
                        TagNameSet tags,
                        Date created,
                        Date modified,
                        Date lastVisited,
                        long visitCount) {
        
        _url = Hope.that(lurl).named("url").isNotNull().value();
        _title = Optional.ofNullable(Strings.safeTrimToNull(title));
        _notes = Optional.ofNullable(Strings.safeTrimToNull(notes));
        _imageUrl = Optional.ofNullable(Strings.safeTrimToNull(imageUrl));
        _tags = (tags == null) ? TagNameSet.EMPTY : tags;
        _created = Optional.ofNullable(created);
        _modified = Optional.ofNullable(modified);
        _lastVisited = Optional.ofNullable(lastVisited);
        _visitCount = Optional.ofNullable(visitCount);
    }
    
    public Lurl lurl() { return _url; }
    public Optional<String> title() { return _title; }
    public Optional<String> notes() { return _notes; }
    public Optional<String> imageUrl() { return _imageUrl; }
    public TagNameSet tagNames() { return _tags; }
    public Optional<Date> created() { return _created; }
    public Optional<Date> modified() { return _modified; }
    public Optional<Date> lastVisited() { return _lastVisited; }
    public Optional<Long> visitCount() { return _visitCount; }
    
    private long cmpCreated() { return _created.map(d -> d.getTime()).orElse(Long.MIN_VALUE); }
    private long cmpLastVisited() { return _lastVisited.map(d -> d.getTime()).orElse(Long.MIN_VALUE); }
    private long cmpModified() { return _modified.map(d -> d.getTime()).orElse(Long.MIN_VALUE); }
    private long cmpVisitCount() { return _visitCount.orElse(0l); }

    public static Builder newBuilder() { return new Builder(); }
    public Builder toBuilder() {
        return newBuilder()
                .url(_url)
                .title(_title.orElse(null))
                .notes(_notes.orElse(null))
                .imageUrl(_imageUrl.orElse(null))
                .tags(_tags)
                .created(_created.orElse(null))
                .modified(_modified.orElse(null))
                .lastVisited(_lastVisited.orElse(null))
                .visitCount(_visitCount.orElse(null));
                    
    }
    
    public static class Builder {
        private Lurl _lurl;
        private String _title, _notes, _imageUrl;
        private TagNameSet _tags;
        private Date _created, _modified, _lastVisited;
        private long _visitCount;
        private Builder() {}
        
        public Builder url(Lurl lurl) {
            _lurl = lurl;
            return this;
        }
        
        public Builder url(String url) {
            _lurl = Lurl.of(url);
            return this;
        }
        
        public Builder title(String title) {
            _title = title;
            return this;
        }
        
        public Builder notes(String notes) {
            _notes = notes;
            return this;
        }
        
        public Builder imageUrl(String imageUrl) {
            _imageUrl = imageUrl;
            return this;
        }
        
        public Builder tags(String tags) {
            _tags = TagNameSet.of(tags);
            return this;
        }
        
        public Builder tags(TagNameSet tags) {
            _tags = tags;
            return this;
        }
        
        public Builder created(Date created) {
            _created = created;
            return this;
        }
        
        public Builder modified(Date modified) {
            _modified = modified;
            return this;            
        }
                
        public Builder lastVisited(Date lastVisited) {
            _lastVisited = lastVisited;
            return this;
        }
        
        public Builder visitCount(long visitCount) {
            _visitCount = visitCount;
            return this;
        }
        
        public Bookmark build() {
            return new Bookmark(_lurl, _title, _notes, _imageUrl, _tags, _created, _modified, _lastVisited, _visitCount);
        }
    }

    public static class GsonAdapter implements JsonDeserializer<Bookmark> {
       
        @Override
        public Bookmark deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
            JsonObject j = je.getAsJsonObject();
            Builder b = newBuilder();
            if (j.has("url")) b.url(j.get("url").getAsString());
            if (j.has("title")) b.title(j.get("title").getAsString());
            if (j.has("notes")) b.notes(j.get("notes").getAsString());
            if (j.has("imageUrl")) b.imageUrl(j.get("imageUrl").getAsString());
            if (j.has("tags")) b.tags((TagNameSet) jdc.deserialize(j.get("tags"), TagNameSet.class));
            if (j.has("created")) b.created((Date) jdc.deserialize(j.get("created"), Date.class));
            if (j.has("modified")) b.modified((Date) jdc.deserialize(j.get("modified"), Date.class));
            if (j.has("lastVisited")) b.lastVisited((Date) jdc.deserialize(j.get("lastVisited"), Date.class));
            if (j.has("visitCount")) b.visitCount(j.get("visitCount").getAsLong());
            return b.build();
        }
        
    }
}
