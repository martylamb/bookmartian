package com.martiansoftware.util;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 *
 * @author mlamb
 */
public class Strings {
    
    public static String safeTrim(String s) {
        return (s == null) ? "" : s.trim();
    }
    
    public static String safeTrimToNull(String s) {
        String result  = safeTrim(s);
        return result.isEmpty() ? null : result;
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
