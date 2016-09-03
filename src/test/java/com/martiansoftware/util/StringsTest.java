package com.martiansoftware.util;

import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mlamb
 */
public class StringsTest {
    
    @Test
    public void testIsEmpty() {
        assertTrue(Strings.isEmpty(""));
        assertTrue(Strings.isEmpty(null));
        assertFalse(Strings.isEmpty(" "));
        assertFalse(Strings.isEmpty("hello"));
    }

    /**
     * Test of safeTrim method, of class Strings.
     */
    @Test
    public void testSafeTrim() {
        assertEquals("", Strings.safeTrim(null));
        assertEquals("", Strings.safeTrim("  \t\n"));
        assertEquals("hello there!", Strings.safeTrim("  hello there! \n"));
    }

    /**
     * Test of safeTrimToNull method, of class Strings.
     */
    @Test
    public void testSafeTrimToNull() {
        assertNull(Strings.safeTrimToNull("     \n\t   \n"));
        assertNull(Strings.safeTrimToNull(null));
        assertEquals("hello there!", Strings.safeTrimToNull("  hello there! \n"));
    }

    /**
     * Test of splitOnWhitespaceAndCommas method, of class Strings.
     */
    @Test
    public void testSplitOnWhitespaceAndCommas() {
        assertEquals(0, Strings.splitOnWhitespaceAndCommas(null).size());
        assertEquals(0, Strings.splitOnWhitespaceAndCommas("").size());
        assertEquals(0, Strings.splitOnWhitespaceAndCommas("  ,, \n,\t,   ,, ,").size());
        
        List<String> split = Strings.splitOnWhitespaceAndCommas("   ,,  hello, \nthere\t,,");
        assertEquals(2, split.size());
        assertEquals("hello", split.get(0));
        assertEquals("there", split.get(1));
    }
    
}
