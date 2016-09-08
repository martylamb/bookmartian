package com.martiansoftware.bookmartian.model;

import com.martiansoftware.bookmartian.model.Bookmark;
import com.martiansoftware.boom.Json;
import java.util.Optional;
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
        JsonConfig.init();
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
}
