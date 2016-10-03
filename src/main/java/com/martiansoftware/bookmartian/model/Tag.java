package com.martiansoftware.bookmartian.model;

import com.martiansoftware.validation.Hope;

/**
 *
 * @author mlamb
 */
public class Tag {

    private final TagName _name;
    private final Color _color;

    private Tag(TagName tagName, Color color) {
        _name = Hope.that(tagName).named("tagName").isNotNull().value();
        _color = (color == null) ? Color.BLACK : color;
    }
    
    public TagName tagName() {
        return _name;
    }
    
    public Color color() {
        return _color;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private TagName _tagName;
        private Color _color;
        
        private Builder() {}
        
        public Builder name(String name) {
            _tagName = TagName.of(name);
            return this;
        }
        
        public Builder color(Color color) {
            _color = color;
            return this;
        }
        
        public Tag build() {
            return new Tag(_tagName, _color);
        }
    }
    
}
