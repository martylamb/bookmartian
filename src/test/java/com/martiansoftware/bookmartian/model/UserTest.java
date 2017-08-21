package com.martiansoftware.bookmartian.model;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mlamb
 */
public class UserTest {
    
    public UserTest() {
    }
    
    @Test public void testUsername() {
        User u = User.newBuilder().username("test user").password("123").build();
        assertEquals("test user", u.username());
        
        u = User.newBuilder().username("  Test USER    ").password("123").build();
        assertEquals("test user", u.username());

        try {
            u = User.newBuilder().password("123").build();
            fail("User accepted a null username");
        } catch (Exception expected) {}
        
        try {
            u = User.newBuilder().username("   ").password("123").build();
            fail("User accepted a whitespace username");
        } catch (Exception expected) {}

        try {
            u = User.newBuilder().username("").password("123").build();
            fail("User accepted an empty username");
        } catch (Exception expected) {}

    }

    @Test public void testPassword() {
        User u = User.newBuilder().username("test user").password("123").build();
        System.out.println(u.pwhash().length());
        assertTrue(u.pwhash().length() > 64);
        System.out.println(u.pwhash());
        
        try {
            u = User.newBuilder().username("test user").build();
            fail("User accepted a null password");
        } catch (Exception expected) {}
        
        // empty password should be acceptable (but not recommended!)
        u = User.newBuilder().username("test user").password("").build();
    }

    @Test public void testAnonymous() {
        assertEquals("anonymous", User.ANONYMOUS.username());
        assertEquals("", User.ANONYMOUS.pwhash());
        assertTrue(User.ANONYMOUS.authenticate(null));
        assertTrue(User.ANONYMOUS.authenticate("abc"));
        assertTrue(User.ANONYMOUS.authenticate(""));
    }
    
    @Test public void testAuth() {
        User u = User.newBuilder().username("test user").password("12345").build();
        assertTrue(u.authenticate("12345"));
        assertFalse(u.authenticate("1234"));
        assertFalse(u.authenticate(""));
        assertFalse(u.authenticate(null));
    }
}
