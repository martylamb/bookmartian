package com.martiansoftware.bookmartian.db;

import com.martiansoftware.bookmartian.model.User;
import com.martiansoftware.bookmartian.model.UserManager;
import java.io.File;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author mlamb
 */
public class DbTests {
    
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();
    
    private static Path tmpPath;
    
    static Database getDb() throws Exception {
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
    
}
