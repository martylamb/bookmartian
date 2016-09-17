package com.martiansoftware.util;

/**
 *
 * @author mlamb
 */
public class Oops {
    
    public static <N> N oops(String fmt, Object... args) {
        throw new RuntimeException(String.format(fmt, args));
    }
    
}
