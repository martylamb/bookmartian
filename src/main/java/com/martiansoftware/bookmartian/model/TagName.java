package com.martiansoftware.bookmartian.model;

import com.martiansoftware.bookmartian.model.JsonConfig.StringAdapter;
import com.martiansoftware.util.Check;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 *
 * @author mlamb
 */
public class TagName implements Comparable<TagName> {

    private static final Pattern VALID_TAG = Pattern.compile("^\\s*(?<tag>[a-zA-Z0-9_-]+)\\s*$");
    
    private final String _name;
    
    private TagName(String name) {
        Matcher m = VALID_TAG.matcher(name);
        if (!m.matches()) {
            throw new IllegalArgumentException("Tags may only consist of letters, numbers, underscores, and dashes.");
        }
       _name = m.group("tag").toLowerCase();
    }
    
    public static TagName of(String name) {
        return new TagName(name);
    }

    @Override
    public String toString() {
        return _name;
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this._name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TagName other = (TagName) obj;
        if (!Objects.equals(this._name, other._name)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(TagName t) {
        return _name.compareTo(t._name);
    }

    // gson helper
    public static class GsonAdapter extends StringAdapter<TagName> {
        @Override protected String toString(TagName t) { return t.toString(); }
        @Override protected TagName fromString(String s) { return TagName.of(s); }
        @Override public Stream<Class> classes() { return Stream.of(TagName.class); }
    }
    
}
