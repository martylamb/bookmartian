package com.martiansoftware.util;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author mlamb
 */
public class Strings {
    
    /**
     * Returns true if String is either null or zero-length.
     * @param s the String to test
     * @return true if String is either null or zero-length.
     */
    public static boolean isEmpty(String s) {
        if (s == null) return true;
        return (s.isEmpty());
    }
    
    /**
     * Trims a String, converting nulls to empty Strings.
     * @param s the String to trim
     * @return the trimmed, not-null String
     */
    public static String safeTrim(String s) {
        return (s == null) ? "" : s.trim();
    }
    
    /**
     * Trims a String, converting empty strings to null
     * @param s the String to trim
     * @return the trimmed, not-empty but possibly null String
     */
    public static String safeTrimToNull(String s) {
        String result  = safeTrim(s);
        return result.isEmpty() ? null : result;
    }
    
    /**
     * Lowercases a String using the root Locale
     * @param s the String to lowercase
     * @return the lowercased String
     */
    public static String lower(String s) {
        if (s == null) return null;
        return s.toLowerCase(Locale.ROOT);
    }
        
    /**
     * Parses a String into a long, possibly throwing a RuntimeException
     * @param s the String to parse into a long
     * @return the parsed long
     */
    public static long asLong(String s) {
        try {
            return Long.valueOf(s);
        } catch (Exception e) {
            return Oops.oops("not a number: '%s'", s);
        }
    }
        
    /**
     * Given a String, splits it around any whitespace or commas and converts
     * it into a List with no empty Strings
     * @param toSplit the String to split
     * @return a List of nonempty Strings that were separated by whitespace
     * or commas
     */
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
