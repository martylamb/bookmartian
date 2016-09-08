package com.martiansoftware.bookmartian.model;

import com.martiansoftware.boom.Json;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mlamb
 */
public class LurlTest {
    
    private void test(String before, String expectedAfter) {
        assertEquals(expectedAfter, Lurl.of(before).toString());
    }
    
    private void test(String beforeAndExpectedAfter) {
        test(beforeAndExpectedAfter, beforeAndExpectedAfter);
    }
    
    @Test
    public void testConstruction() {
        test("martiansoftware.com");
        test("MartianSoftware.com");
        
        test("Http://martiansoftware.com",
             "http://martiansoftware.com");
        
        test("File://Path/to/some/File.txt",
             "file://Path/to/some/File.txt");
        
        test("http://MartianSoftware.com", "http://martiansoftware.com");
        
        test("HTTP://amazon.com",
             "http://amazon.com");
        
        test("HTTPs://MartyLamb:p@$$word@SomeWebsite.org/Mixed/Case/Path?with==params#andAnchors",
             "https://MartyLamb:p@$$word@somewebsite.org/Mixed/Case/Path?with==params#andAnchors");
        
        test("mailTo://Fake@email.fake",
             "mailto://fake@email.fake");
    }
    
    @Test
    public void testJson() {
        JsonConfig.init();
        
        Lurl l = Lurl.of("http://martiansoftware.com");
        String json = Json.toJson(l);        
        System.out.println(json);
        Lurl l2 = Json.fromJson(json, Lurl.class);
        assertEquals("http://martiansoftware.com", l2.toString());
    }
}
