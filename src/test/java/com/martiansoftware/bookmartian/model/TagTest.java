package com.martiansoftware.bookmartian.model;

import com.martiansoftware.bookmartian.Colors;
import com.martiansoftware.bookmartian.model.Tag;
import com.martiansoftware.boom.Json;
import java.awt.Color;
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
        assertEquals("abc", t.tagName().toString());
        assertEquals(Color.BLACK, t.color());

        t = Tag.newBuilder().name(" Test_Bookmark   ").color(Color.RED).build();
        assertEquals("test_bookmark", t.tagName().toString());
        assertEquals(Color.RED, t.color());
        
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
        
        Tag t = Tag.newBuilder().name("test-tag").color(Color.BLUE).build();
        String json = Json.toJson(t);
        System.out.println(json);
        
        Tag t2 = Json.fromJson(json, Tag.class);
        assertEquals("test-tag", t2.tagName().toString());
        assertEquals(Color.BLUE, t2.color());
    }
}
