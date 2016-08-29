package com.martiansoftware.bookmartian;

import com.martiansoftware.util.Check;
import com.martiansoftware.util.Strings;

/**
 *
 * @author mlamb
 */
public class Tag implements Comparable<Tag> {

    private final String _name, _color;

    private Tag(String name, String color) {
        _name = Check.arg(Strings.safeTrim(name), "name")
                    .notNullOrEmpty()
                    .value();
        Check.arg(_name, "name").isTrue(_name.matches("^[^\\s,]+$"), "Tag name may not contain whitespace or commas.");
        
        _color = Colors.safeColor(color).toLowerCase();
    }
    
    public String name() { return _name; }
    public String color() { return _color; }
    public String normalizedName() { return normalizeName(_name); }

    public static String normalizeName(String tagName) { return Strings.safeTrim(tagName).toLowerCase(); }
    
    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public int compareTo(Tag t) {
        return normalizedName().compareTo(t.normalizedName());
    }
    
    public static class Builder {
        private String _name;
        private String _color;
        
        private Builder() {}
        
        public Builder name(String name) {
            _name = name;
            return this;
        }
        
        public Builder color(String color) {
            _color = color;
            return this;
        }
        
        public Tag build() {
            return new Tag(_name, _color);
        }
    }
    
}
