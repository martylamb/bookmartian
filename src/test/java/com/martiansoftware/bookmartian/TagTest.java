package com.martiansoftware.bookmartian;

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
public class TagTest {
    
    public TagTest() {
    }
    
    /**
     * Test of name method, of class Tag.
     */
    @Test
    public void testName() {
        Tag t = Tag.newBuilder().name("abc").build();
        assertEquals("abc", t.name());
        assertEquals("abc", t.normalizedName());
        assertTrue(Colors.isColor(t.color()));

        t = Tag.newBuilder().name(" Test_Bookmark   ").color("#000000").build();
        assertEquals("Test_Bookmark", t.name());
        assertEquals("test_bookmark", t.normalizedName());
        assertEquals("#000000", t.color());
        
        try {
            t = Tag.newBuilder().name(null).build();
            fail("Accepted null name");
        } catch (Exception expected) {}
        
        try {
            t = Tag.newBuilder().name("test bm").build();
            fail("Accepted whitespace");
        } catch (Exception expected) {}
        
    }

}
