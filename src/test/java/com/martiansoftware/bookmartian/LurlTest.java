/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.martiansoftware.bookmartian;

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
public class LurlTest {
    
    private void test(String before, String expectedAfter) {
        assertEquals(expectedAfter, new Lurl(before).toString());
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
    public void testId() {
        assertEquals("66064c6105dee6a177a637f060174c01f8c549ad", new Lurl("Https://MartianSoftware.com").id());
        assertEquals("66064c6105dee6a177a637f060174c01f8c549ad", new Lurl("https://martiansoftware.com").id());
        assertEquals("64e2ad35b5d0bf7b4dd0d832831428a9ff14d3c1", new Lurl("http://martiansoftware.com").id());
    }
}
