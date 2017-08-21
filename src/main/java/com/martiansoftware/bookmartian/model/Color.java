package com.martiansoftware.bookmartian.model;

import com.martiansoftware.util.Strings;
import com.martiansoftware.validation.Hope;
import java.util.Objects;
import java.util.stream.Stream;

/**
 *
 * @author mlamb
 */
public class Color implements Comparable<Color> {

    public static final Color BLACK = Color.of("#000000");
    
    private final String _color;

    private Color(String color) {
        _color = Strings.lower(
                    Hope.that(color)
                    .named("color")
                    .isNotNullOrEmpty()
                    .matches("^#[0-9a-zA-Z]{6}$")
                    .value()
        );
    }
    
    public static Color of(String hex) {
        return new Color(hex);
    }
    
    @Override
    public String toString() {
        return _color;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(this._color);
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
        final Color other = (Color) obj;
        if (!Objects.equals(this._color, other._color)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Color t) {
        return _color.compareTo(t._color);
    }

    // gson helper
    public static class GsonAdapter extends JsonConfig.StringAdapter<Color> {
        @Override protected String toString(Color color ){ return color.toString(); }
        @Override protected Color fromString(String s) { return Color.of(s); }
        @Override public Stream<Class> classes() { return Stream.of(Color.class); }
    }
    
}
