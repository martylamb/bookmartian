package com.martiansoftware.bookmartian.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
        Json.init();
        
        Tag t = Tag.newBuilder().name("test-tag").color(BLUE).build();
        String json = Json.toJson(t);
        System.out.println(json);
        
        Tag t2 = Json.fromJson(json, Tag.class);
        assertEquals("test-tag", t2.tagName().toString());
        assertEquals(BLUE, t2.color());
    }
    
    @Test
    public void testRoundTrip() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        Tag t1 = Tag.newBuilder().name("test-tag").color(Color.of("#123456")).build();
        oout.writeObject(t1.toBuilder());
        oout.close();
        byte[] b = bout.toByteArray();
        
        ByteArrayInputStream bin = new ByteArrayInputStream(b);
        ObjectInputStream oin = new ObjectInputStream(bin);
        Tag t2 = ((Tag.Builder) oin.readObject()).build();
        
        assertEquals(0, t1.compareTo(t2));
    }
}
