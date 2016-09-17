package com.martiansoftware.util;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author mlamb
 */
public class Strings {
    
    public static boolean isEmpty(String s) {
        if (s == null) return true;
        return (s.isEmpty());
    }
    
    public static String safeTrim(String s) {
        return (s == null) ? "" : s.trim();
    }
    
    public static String safeTrimToNull(String s) {
        String result  = safeTrim(s);
        return result.isEmpty() ? null : result;
    }
    
    public static String lower(String s) {
        if (s == null) return null;
        return s.toLowerCase(Locale.ROOT);
    }
        
    public static long asLong(String s) {
        try {
            return Long.valueOf(s);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("not a number: '%s'", s));
        }
    }
        
    public static List<String> splitOnWhitespaceAndCommas(String toSplit) {
        List<String> result = new java.util.ArrayList<>();
        if (toSplit != null) {
            String[] splits = toSplit.split("[\\s,]+");
            for (String split : splits) {
                if (!split.isEmpty()) result.add(split);
            }
        }
        return Collections.unmodifiableList(result);
    }
    
}
