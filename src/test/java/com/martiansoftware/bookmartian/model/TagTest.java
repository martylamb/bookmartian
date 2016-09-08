package com.martiansoftware.bookmartian.model;

import com.martiansoftware.boom.Json;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mlamb
 */
public class TagTest {
    
    private static final Color RED = Color.of("#ff0000");
    private static final Color BLUE = Color.of("#0000ff");
    
    public TagTest() {
    }
    
    /**
     * Test of name method, of class Tag.
     */
    @Test
    public void testName() {
        Tag t = Tag.newBuilder().name("abc").build();
        assertEquals("abc", t.tagName().toString());
        assertEquals(Color.BLACK, t.color());

        t = Tag.newBuilder().name(" Test_Bookmark   ").color(RED).build();
        assertEquals("test_bookmark", t.tagName().toString());
        assertEquals(RED, t.color());
        
        try {
            t = Tag.newBuilder().name(null).build();
            fail("Accepted null name");
        } catch (Exception expected) {}
        
        try {
            t = Tag.newBuilder().name("test bm").build();
            fail("Accepted whitespace");
        } catch (Exception expected) {}        
    }

    @Test
    public void testJson() {
        JsonConfig.init();
        
        Tag t = Tag.newBuilder().name("test-tag").color(BLUE).build();
        String json = Json.toJson(t);
        System.out.println(json);
        
        Tag t2 = Json.fromJson(json, Tag.class);
        assertEquals("test-tag", t2.tagName().toString());
        assertEquals(BLUE, t2.color());
    }
}
