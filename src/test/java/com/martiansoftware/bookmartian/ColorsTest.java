package com.martiansoftware.bookmartian;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mlamb
 */
public class ColorsTest {
    
    public ColorsTest() {
    }
    
    @Test
    public void testIsColor() {
        assertTrue(Colors.isColor("#aabbcc"));
        assertFalse(Colors.isColor(null));
        assertFalse(Colors.isColor(" #aabbcc"));
        assertFalse(Colors.isColor("#hello"));
        assertFalse(Colors.isColor("#aabbccd"));
    }

    @Test
    public void testRandom() {
        assertEquals(7, Colors.random().length());
        assertTrue(Colors.random().startsWith("#"));        
    }

    @Test
    public void testSafeColor() {
        assertEquals("#aabbcc", Colors.safeColor("#aabbcc"));
        assertEquals("#000000", Colors.safeColor("   #000000       \n"));
        assertTrue(Colors.isColor(Colors.safeColor(null)));
    }
    
}
