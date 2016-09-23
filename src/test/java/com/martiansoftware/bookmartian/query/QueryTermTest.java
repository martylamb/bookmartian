package com.martiansoftware.bookmartian.query;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mlamb
 */
public class QueryTermTest {
    
    private void test(String action, String arg, String expected) {
        assertEquals(expected, QueryTerm.of(action, arg).toString());
    }
    
    @Test
    public void testToString() {
        test("ABC", "def", "abc:def");
        test("multiple", "things with spaces", "multiple:\"things with spaces\"");
        test("has", "commas,too", "has:\"commas,too\"");
        
        
    }
    
}
