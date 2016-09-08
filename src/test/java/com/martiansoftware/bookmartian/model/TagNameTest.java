package com.martiansoftware.bookmartian.model;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mlamb
 */
public class TagNameTest {
    
    public TagNameTest() {
    }
    
    /**
     * Test of of method, of class TagName.
     */
    @Test
    public void testGoodTagNames() {
        TagName t1 = TagName.of(" HeLlO\n");
        assertEquals("hello", t1.toString());
        
        TagName t2 = TagName.of("test");
        assertEquals("test", t2.toString());
        
        TagName t3 = TagName.of("   TEST\n");
        assertEquals(t2, t3);
        
        TagName t4 = TagName.of("This-is_a_TEST");
        assertEquals("this-is_a_test", t4.toString());
    }

    private void expectIAEorNPE(Runnable r) {
        try {
            r.run();
            fail("Expected an IllegalArgumentException");
        } catch (IllegalArgumentException | NullPointerException expected) {}
    }
    
    @Test
    public void testBadTagNames() {
        expectIAEorNPE(() -> { TagName t = TagName.of(null); });
        expectIAEorNPE(() -> { TagName t = TagName.of(""); }); 
        expectIAEorNPE(() -> { TagName t = TagName.of("   \t"); }); 
        expectIAEorNPE(() -> { TagName t = TagName.of("a test"); }); 
        expectIAEorNPE(() -> { TagName t = TagName.of("#tag"); }); 
    }
    
    @Test
    public void testCompareTo() {
        TagName t1 = TagName.of("aardvark");
        TagName t2 = TagName.of("zebra");
        assertTrue(t1.compareTo(t2) < 0);
        assertTrue(t2.compareTo(t1) > 0);
        
        TagName t3 = TagName.of("AARDVARK");
        assertTrue(t1.compareTo(t3) == 0);
        assertTrue(t3.compareTo(t1) == 0);
    }
}
