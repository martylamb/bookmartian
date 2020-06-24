package com.martiansoftware.bookmartian.model;

import com.martiansoftware.util.ObjectIO;
import com.martiansoftware.validation.Hope;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author mlamb
 */
public class Tag implements Comparable<Tag> {

    private final TagName _name;
    private final Color _color;

    private Tag(TagName tagName, Color color) {
        _name = Hope.that(tagName).named("tagName").isNotNull().value();
        _color = (color == null) ? Color.BLACK : color; // TODO: randomly choose a color?
    }
    
    public static Tag of(TagName tagName) {
        return new Tag(tagName, null);
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

    @Override
    public int compareTo(Tag t) {
        int result = _name.compareTo(t._name);
        if (result == 0) {
            result = _color.compareTo(t._color);
        }
        return result;
    }

    public Builder toBuilder() {
        Builder result = new Builder();
        result._tagName = _name;
        result._color = _color;
        return result;
    }
    
    public static class Builder implements Externalizable {
        private static final long serialVersionUID = 0; // used a fixed serial ID as far as java is concerned...
        private static final long MYVERSION = 0;        // but use our own to handle backwards compatibility manually
        
        private TagName _tagName;
        private Color _color;
        
        public Builder() {}
        
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

        @Override
        public void writeExternal(ObjectOutput oo) throws IOException {
            oo.writeLong(MYVERSION);
            ObjectIO.writeNullableUTF(oo, _tagName == null ? null : _tagName.toString());
            ObjectIO.writeNullableUTF(oo, _color == null ? null : _color.toString());
        }

        @Override
        public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
            ObjectIO.assertMaxVersion(oi, MYVERSION);
            _tagName = ObjectIO.fromNullableUTF(oi, s -> TagName.of(s));
            _color = ObjectIO.fromNullableUTF(oi, s -> Color.of(s));
        }
    }
    
}
