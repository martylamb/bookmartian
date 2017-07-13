/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.martiansoftware.bookmartian.db;

import com.martiansoftware.bookmartian.model.Bookmark;
import com.martiansoftware.bookmartian.model.Bookmartian;
import com.martiansoftware.bookmartian.model.Lurl;
import com.martiansoftware.bookmartian.model.Tag;
import com.martiansoftware.bookmartian.model.TagName;
import com.martiansoftware.bookmartian.model.User;
import com.martiansoftware.bookmartian.model.UserManager;
import java.util.Collection;
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
public class DbBookmartianIntegrationTest extends DbTests {
    
    private User user(String s) throws Exception {
        UserManager um = getDb().userManager();
        
        Optional<User> ou = um.get(s);
        if (ou.isPresent()) return ou.get();
        
        User u = User.newBuilder().username(s).password("password-" + s).build();
        um.put(u);
        return u;
    }
    
    private Bookmartian testBookmartian(String testname) throws Exception {
        return getDb().bookmartianFor(user(testname));   
    }
    
    /**
     * Test of bookmarks method, of class DbBookmartian.
     */
    @Test
    public void testBookmarks() throws Exception {
        Bookmartian dbb = testBookmartian("testBookmarks");
        assertEquals(0, dbb.bookmarks().size());
        assertEquals(0, dbb.tags().size());
        
        Bookmark b1 = Bookmark.newBuilder()
                        .imageUrl("some/image")
                        .url("http://some/site")
                        .tags("a b c")
                        .build();
        
        dbb.update(null, b1);

        assertEquals(1, dbb.bookmarks().size());
        assertEquals(3, dbb.tags().size());
        for (int i = 0; i < 10; ++i) System.out.println("****************************************");
    }
    
    @Test
    public void testTags() throws Exception {
        Bookmartian dbb = testBookmartian("testTags");
        
        assertEquals(0, dbb.tags().size());
        Bookmark b1 = Bookmark.newBuilder()
                        .url("http://testing.tags")
                        .tags("tag1 TAG2 tag2")
                        .build();
        
        dbb.update(null, b1);
        
        assertEquals(2, dbb.tags().size());
        assertTrue(dbb.get(TagName.of("tag1")).isPresent());
        assertTrue(dbb.get(TagName.of("tag2")).isPresent());
        assertFalse(dbb.get(TagName.of("tag3")).isPresent());
        
        
        
    }
    
}
