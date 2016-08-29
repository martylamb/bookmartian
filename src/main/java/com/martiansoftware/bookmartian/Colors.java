package com.martiansoftware.bookmartian;

import com.martiansoftware.util.Strings;
import java.util.Random;

/**
 * this is dumb and will be replaced.
 * @author mlamb
 */
public class Colors {

    public static boolean isColor(String s) {
        if (s == null) return false;
        return s.matches("^#[0-9a-fA-F]{6}$");
    }
    
    public static String random() {
        byte[] b = new byte[3];
        new Random().nextBytes(b);        
        return String.format("#%02x%02x%02x", b[0], b[1], b[2]);
    }
    
    public static String safeColor(String s) {
        String color = Strings.safeTrim(s);
        return isColor(color) ? color : random();
    }
}
