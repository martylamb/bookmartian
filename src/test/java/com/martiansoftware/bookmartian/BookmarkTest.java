package com.martiansoftware.bookmartian;

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
        Bookmark b = Bookmark.newBuilder().url("http://martylamb.com").build();
        assertEquals("http://martylamb.com", b.url());

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
        
        assertEquals("http://martiansoftware.com", b.url());
        assertEquals("notes go here", b.notes().get());
        assertEquals("title here", b.title().get());
        assertEquals("pic/of/me", b.imageUrl().get());
    }

}
