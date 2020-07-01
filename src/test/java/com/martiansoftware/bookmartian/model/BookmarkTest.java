package com.martiansoftware.bookmartian.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Optional;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mlamb
 */
public class BookmarkTest {
    
    public BookmarkTest() {
    }
    
    @Test
    public void testBookmark() {
        Bookmark b = Bookmark.newBuilder().url("http://MartyLamb.com").build();
        assertEquals("http://martylamb.com", b.lurl().toString());

        try {
            b = Bookmark.newBuilder().build();
            fail("Accepted bookmark without url");
        } catch (Exception expected) {}

        try {
            b = Bookmark.newBuilder().url("     \t\n    ").build();
            fail("Accepted bookmark consisting of only whitespace");
        } catch (Exception expected) {}

        b = Bookmark.newBuilder()
                .url("http://martiansoftware.com")
                .notes("notes go here")
                .title("title here")
                .imageUrl("pic/of/me")
                .build();
        
        assertEquals("http://martiansoftware.com", b.lurl().toString());
        assertEquals("notes go here", b.notes().get());
        assertEquals("title here", b.title().get());
        assertEquals("pic/of/me", b.imageUrl().get());
        
        b = Bookmark.newBuilder().url("something").build();
        assertTrue(b.tagNames().isEmpty());
        assertEquals(b.tagNames(), TagNameSet.EMPTY);
    }

    @Test
    public void testJson() {
        Json.init();
        Bookmark b = Bookmark.newBuilder()
                .url("http://martiansoftware.com")
                .notes("notes go here")
                .imageUrl("pic/of/me")
                .build();
        
        String json = Json.toJson(b);
        System.out.println(json);
        
        b = Json.fromJson(json, Bookmark.class);
        assertEquals("http://martiansoftware.com", b.lurl().toString());
        assertEquals("notes go here", b.notes().get());
        assertEquals("pic/of/me", b.imageUrl().get());
    }
    
    @Test
    public void testMerge() {
        long d = System.currentTimeMillis();
        Date c1 = new Date(d++), v1 = new Date(d++);
        Bookmark bm1 = Bookmark.newBuilder().title("b1").url("http://a").created(c1).lastVisited(v1).visitCount(100).build();
        
        Date c2 = new Date(d++), v2 = new Date(d++);
        Bookmark bm2 = Bookmark.newBuilder().title("b2").url("http://b").created(c2).lastVisited(v2).visitCount(50).build();

        Bookmark bm3 = bm1.merge(Optional.of(bm2));
        assertEquals(c1, bm3.created().get()); // should be oldest
        assertEquals(v2, bm3.lastVisited().get()); // should be most recent
        assertEquals(100, bm3.visitCount().get().longValue()); // should be highest
    }


    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        Date d = new Date();
        Bookmark b = Bookmark.newBuilder()
                .url("http://martiansoftware.com")
                .notes("notes go here")
                .imageUrl("pic/of/me")
                .tags("these are my tags")
                .visitCount(42)
                .created(d)
                .build();
        
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);        
        oout.writeObject(b.toBuilder());
        
        byte[] bytes = bout.toByteArray();
        ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
        ObjectInputStream oin = new ObjectInputStream(bin);
        Bookmark b2 = ((Bookmark.Builder) oin.readObject()).build();
        
        assertEquals(b.created(), b2.created());
        assertEquals(b.imageUrl(), b2.imageUrl());
        assertEquals(b.tagNames().toString(), b2.tagNames().toString());
        assertEquals(new Long(42), b2.visitCount().get());
        assertEquals(d, b2.created().get());
        assertFalse(b2.lastVisited().isPresent());        
    }

}
