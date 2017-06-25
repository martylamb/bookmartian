/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.martiansoftware.bookmartian.db;

import com.martiansoftware.bookmartian.model.User;
import com.martiansoftware.bookmartian.model.UserManager;
import java.util.Optional;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mlamb
 */
public class DbUserManagerTest extends DbTests {
        
    /**
     * Test of get method, of class DbUserManager.
     */
    @Test
    public void testGet() throws Exception {
        try (Database db = getDb()) {
            UserManager um = db.userManager();
            assertEquals("user3", um.get("user3").get().username());
            assertEquals(Optional.empty(), um.get("user999"));
        }
    }

    /**
     * Test of put method, of class DbUserManager.
     */
    @Test
    public void testPut() throws Exception {
        try (Database db = getDb()) {
            UserManager um = db.userManager();
            User u5 = um.get("user5").get();
            assertTrue(u5.authenticate("pw5"));
            
            um.put(u5.toBuilder().password("password5").build());
            
            u5 = um.get("user5").get();
            assertFalse(u5.authenticate("pw5"));
            assertTrue(u5.authenticate("password5"));
            
            User u555 = User.newBuilder().username("user555").password("pw555").build();
            um.put(u555);
        }

        try (Database db = getDb()) {
            UserManager um = db.userManager();
            assertTrue(um.get("user5").get().authenticate("password5"));
            assertTrue(um.get("user555").get().authenticate("pw555"));
        }
    }

    /**
     * Test of remove method, of class DbUserManager.
     */
    @Test
    public void testRemove() throws Exception {
        try (Database db = getDb()) {
            UserManager um = db.userManager();
            User userToDelete = User.newBuilder().username("deleteme").password("deleteme").build();
            um.put(userToDelete);
        }

        try (Database db = getDb()) {
            UserManager um = db.userManager();
            User userToDelete = um.get("deleteme").get();
            db.userManager().remove(userToDelete);
        }

        try (Database db = getDb()) {
            UserManager um = db.userManager();
            assertFalse(um.get("deleteme").isPresent());
        }
    }
    
}
