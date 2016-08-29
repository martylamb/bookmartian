package com.martiansoftware.bookmartian;

import com.martiansoftware.util.Check;
import com.martiansoftware.util.Strings;
import java.util.Optional;

/**
 *
 * @author mlamb
 */
public class Bookmark implements Comparable<Bookmark> {
    private final String _url; // TODO: reify a lenient URL that normalizes schema and domain name
    private final Optional<String> _title, _notes, _imageUrl;
    private final TagRefs _tags;
    
    
    private Bookmark(String url, String title, String notes, String imageUrl, TagRefs tags) {
        _url = Check.arg(Strings.safeTrim(url), "url")
            .notNullOrEmpty()
            .value();

        _title = Optional.ofNullable(Strings.safeTrimToNull(title));
        _notes = Optional.ofNullable(Strings.safeTrimToNull(notes));
        _imageUrl = Optional.ofNullable(Strings.safeTrimToNull(imageUrl));
        _tags = (tags == null) ? TagRefs.EMPTY : tags;
    }
    
    public String url() { return _url; }
    public Optional<String> title() { return _title; }
    public Optional<String> notes() { return _notes; }
    public Optional<String> imageUrl() { return _imageUrl; }
    public TagRefs tags() { return _tags; }
    
    public static Builder newBuilder() { return new Builder(); }

    @Override
    public int compareTo(Bookmark t) {
        return url().compareTo(t.url());
    }
    
    public static class Builder {
        private String _url, _title, _notes, _imageUrl;
        private TagRefs _tags;
        
        private Builder() {}
        
        public Builder url(String url) {
            _url = url;
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
            _tags = new TagRefs(tags);
            return this;
        }
        
        public Bookmark build() {
            return new Bookmark(_url, _title, _notes, _imageUrl, _tags);
        }
    }
}
