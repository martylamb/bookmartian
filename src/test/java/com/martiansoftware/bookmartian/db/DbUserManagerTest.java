/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.martiansoftware.bookmartian.db;

import com.martiansoftware.bookmartian.model.User;
import com.martiansoftware.bookmartian.model.UserManager;
import java.io.File;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author mlamb
 */
public class DbUserManagerTest {
    
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();
    
    private static Path tmpPath;
    
    public DbUserManagerTest() {
    }
    
    
    private static Database getDb() throws Exception {
        return new Database(tmpPath.resolve("testdb"));
    }
    
    @BeforeClass
    public static void setUpClass() {
        try {
            tmpPath = Files.createTempDirectory(DbUserManagerTest.class.getName());
            System.out.format("Created temp directory %s%n", tmpPath.toFile().getAbsolutePath());
            try (Database db = getDb()) {
                UserManager um = db.userManager();
                for (int i = 0; i < 10; ++i) {
                    um.put(User.newBuilder()
                            .username("user" + i)
                            .password("pw" + i)
                            .build());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }    
    
    @AfterClass
    public static void tearDownClass() {
        try {
            Files.walk(tmpPath, FileVisitOption.FOLLOW_LINKS)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .peek(f -> System.out.format("Deleting %s%n", f.getAbsolutePath()))
                .forEach(File::delete);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
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
