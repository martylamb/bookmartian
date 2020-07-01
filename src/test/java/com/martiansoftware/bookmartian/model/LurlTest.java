package com.martiansoftware.bookmartian.model;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mlamb
 */
public class LurlTest {
    
    private void test(String before, String expectedAfter) {
        assertEquals(expectedAfter, Lurl.of(before).toString());        
        assertEquals(Lurl.of(before), Lurl.of(expectedAfter));
    }
    
    @Test
    public void testConstruction() {
        test("martiansoftware.com",
             "http://martiansoftware.com");
        
        test("MartianSoftware.com",
             "http://martiansoftware.com");
        
        test("Http://martiansoftware.com",
             "http://martiansoftware.com");
        
        test("File://Path/to/some/File.txt",
             "file://Path/to/some/File.txt");
        
        test("http://MartianSoftware.com", "http://martiansoftware.com");
        
        test("HTTP://amazon.com",
             "http://amazon.com");
        
        test("HTTPs://MartyLamb:p@$$word@SomeWebsite.org/Mixed/Case/Path?with==params#andAnchors",
             "https://MartyLamb:p@$$word@somewebsite.org/Mixed/Case/Path?with==params#andAnchors");
        
        test("mailTo://Fake@email.fAKe",
             "mailto://fake@email.fake");
        
        test("http://stupid.annoying.trailing.slash/",
             "http://stupid.annoying.trailing.slash");

        test("another/",
             "http://another");
        
        test("https://trailing-slash.com/a/b/c/",
             "https://trailing-slash.com/a/b/c");
        
        test("http://slash.With.query/aBc/?Query=something",
             "http://slash.with.query/aBc/?Query=something");
        
        test("http://slash.With.query/aBc/?Query=something",
             "http://slash.with.query/aBc/?Query=something");
        
        test("a:weirdUrl", "a:weirdUrl");
        
        test("\\\\?\\UNC\\myServer\\Path\\To\\Thing",
             "\\\\?\\UNC\\myserver\\Path\\To\\Thing");
        
        test("\\\\?\\UNC\\myServer",
             "\\\\?\\UNC\\myserver");
        
        test("\\\\?\\UNC\\myServer\\",
             "\\\\?\\UNC\\myserver");

        test("\\\\?\\UNC\\myServer\\A\\B\\c\\",
             "\\\\?\\UNC\\myserver\\A\\B\\c");

        test("\\\\Server",
             "\\\\server");
        
        test("\\\\Server\\",
             "\\\\server");
        
        test("\\\\Server\\a\\b\\c\\",
             "\\\\server\\a\\b\\c");

        test("\\\\Server\\a\\b\\c",
             "\\\\server\\a\\b\\c");
        
        test("\\\\", "\\\\");
        test("\\\\\\noServer",
             "\\\\\\noServer");
    }
    
    @Test
    public void testJson() {
        Json.init();
        
        Lurl l = Lurl.of("http://martiansoftware.com");
        String json = Json.toJson(l);        
        System.out.println(json);
        Lurl l2 = Json.fromJson(json, Lurl.class);
        assertEquals("http://martiansoftware.com", l2.toString());
    }
}
