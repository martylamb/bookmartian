package com.martiansoftware.bookmartian.model;

import com.martiansoftware.bookmartian.model.TagNameSet;
import com.martiansoftware.util.Check;
import com.martiansoftware.util.Strings;
import java.util.Optional;

/**
 *
 * @author mlamb
 */
public class Bookmark {
    private final Lurl _lurl;
    private final Optional<String> _title, _notes, _imageUrl;
    private final TagNameSet _tags;
    
    
    private Bookmark(Lurl lurl, String title, String notes, String imageUrl, TagNameSet tags) {
        _lurl = Check.arg(lurl, "url").notNull().value();

        _title = Optional.ofNullable(Strings.safeTrimToNull(title));
        _notes = Optional.ofNullable(Strings.safeTrimToNull(notes));
        _imageUrl = Optional.ofNullable(Strings.safeTrimToNull(imageUrl));
        _tags = (tags == null) ? TagNameSet.EMPTY : tags;
    }
    
    public Lurl lurl() { return _lurl; }
    public Optional<String> title() { return _title; }
    public Optional<String> notes() { return _notes; }
    public Optional<String> imageUrl() { return _imageUrl; }
    public TagNameSet tagNames() { return _tags; }
    
    public static Builder newBuilder() { return new Builder(); }

    
    public static class Builder {
        private Lurl _lurl;
        private String _title, _notes, _imageUrl;
        private TagNameSet _tags;
        
        private Builder() {}
        
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
        
        public Bookmark build() {
            return new Bookmark(_lurl, _title, _notes, _imageUrl, _tags);
        }
    }
}
