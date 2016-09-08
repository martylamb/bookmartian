package com.martiansoftware.bookmartian.model;

import java.io.File;
import java.util.Map;
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
public class JsonDirMapTest {
    
    public JsonDirMapTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();
    
    /**
     * Test of scan method, of class JsonDir.
     */
    @Test
    public void testScan() throws Exception {
        
        File d = tmp.newFolder();
        
        JsonDirMap<String, TestThing> jd = JsonDirMap.newBuilder()
                                            .path(d.toPath())
                                            .valueClass(TestThing.class)
                                            .keyGetter(t -> ((TestThing) t).key)
                                            .build();

        for (int i = 0; i < 5; ++i) {
            TestThing tt = new TestThing();
            tt.key = "item " + i;
            tt.i = i;
            jd.add(tt);
        }

        JsonDirMap<String, TestThing> jd2 = JsonDirMap.newBuilder()
                                            .path(d.toPath())
                                            .valueClass(TestThing.class)
                                            .keyGetter(t -> ((TestThing) t).key)
                                            .build();
        
        jd2.remove("item 2");

        jd = JsonDirMap.newBuilder()
                    .path(d.toPath())
                    .valueClass(TestThing.class)
                    .keyGetter(t -> ((TestThing) t).key)
                    .build();        
        
        assertEquals(4, jd.size());
        assertNotNull(jd.get("item 0"));
        assertNotNull(jd.get("item 1"));
        assertNull(jd.get("item 2"));
        assertNotNull(jd.get("item 3"));
        assertNotNull(jd.get("item 4"));
    }
    
    private class TestThing {
        public String key;
        public int i;
        public String s = "abc";
        @Override public String toString() {
            return String.format("key=[%s], id=%d, s=[%s]", key, i, s);
        }
    }
}
