package com.martiansoftware.bookmartian.model;

import com.martiansoftware.util.Check;

/**
 *
 * @author mlamb
 */
public class Tag {

    private final TagName _tagName;
    private final Color _color;

    private Tag(TagName tagName, Color color) {
        _tagName = Check.arg(tagName, "tagName").notNull().value();
        _color = (color == null) ? Color.BLACK : color;
    }
    
    public TagName tagName() {
        return _tagName;
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
