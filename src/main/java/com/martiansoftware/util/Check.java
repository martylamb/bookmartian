package com.martiansoftware.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

/**
 * 
 * @author mlamb
 */
public class Check <T> {

    private enum OOPSER {
        ARG, STATE;
        public void oops(String msg) {
            if (this == ARG) throw new IllegalArgumentException(msg);
            throw new IllegalStateException(msg);
        }
    };

    private final T _value;
    private final String _name;
    private final OOPSER _oopser;
    
    private Check(T o, String name, OOPSER oopser) {
        _value = o;
        _name = name;
        _oopser = oopser;
    }
    
    public static <T> Check<T> arg(T value) { return arg(value, "value"); }
    public static <T> Check<T> arg(T value, String name) { return new Check(value, name, OOPSER.ARG); }
    
    public static <T> Check<T> state(T value) { return state(value, "value"); }
    public static <T> Check<T> state(T value, String name) { return new Check(value, name, OOPSER.STATE); }
    
    public Check<T> notNull() {
        if (_value == null) throw new NullPointerException(String.format("%s may not be null", _name));
        return this;
    }

    private boolean _isPresent() {
        notNull();
        if (_value instanceof Optional) return ((Optional) _value).isPresent();
        else oops("%s is not an Optional; check for isPresent() is not valid.", _name);
        return false; // not reachable but needed to satisfy compiler
    }
    
    public Check<T> isNotPresent() {
        if (_isPresent()) oops("%s must not be present", _name);
        return this;
    }
    
    public Check<T> isPresent() {
        if (!_isPresent()) oops("%s must be present", _name);
        return this;
    }
    
    public Check<T> notNullOrEmpty() {
        notNull();
        boolean oops = false;
        if (_value.getClass().isArray()) {
            oops = ((Object[]) _value).length == 0;
        } else {
            try {
                Method m = _value.getClass().getMethod("isEmpty");
                if (m.getReturnType().equals(Boolean.TYPE)) {
                    oops = (Boolean) m.invoke(_value);
                } else throw new NoSuchMethodException("unexpected return type: " + m.getReturnType());
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new IllegalStateException(String.format("class %s does not provide an accessible boolean isEmpty() method", _value.getClass().getName()), e);
            } catch (InvocationTargetException e) {
                throw new IllegalStateException(String.format("unable to invoke %s.isEmpty()", _value.getClass().getName()), e);
            }
        }
        if (oops) oops("%s may not be empty", _name);
        return this;
    }

    public Check<T> notEquals(Object other) {
        if (Objects.equals(_value, other)) oops("%s may not equal '%s'", _name, other);
        return this;
    }
    
    public Check<T> isFalse(boolean condition, String fmt, Object... args) {
        if (condition) oops(fmt, args);
        return this;
    }
    
    public Check<T> isTrue(boolean condition, String fmt, Object... args) {
        if (!condition) oops(fmt, args);
        return this;
    }

    public T value() { return _value; }
    
    private void oops(String fmt, Object... args) {
        _oopser.oops(String.format(fmt, args));
    }
    
}
