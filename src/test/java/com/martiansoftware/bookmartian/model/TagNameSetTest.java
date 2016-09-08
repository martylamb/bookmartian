package com.martiansoftware.bookmartian.model;

import com.martiansoftware.boom.Json;
import java.util.Set;
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
public class TagNameSetTest {
    
    public TagNameSetTest() {
    }
    
    @Test
    public void testTagNameSet() {
        TagNameSet t = TagNameSet.of("one, two, three four");
        assertEquals(4, t.asSet().size());
        assertFalse(t.isEmpty());
        assertTrue(t.containsAll("one four"));
        assertTrue(t.containsAll("three three three three"));
        assertTrue(t.containsAll("TWO four"));
        assertFalse(t.containsAll("one four five"));
    }

    @Test
    public void testJsonRoundTrip() {
        JsonConfig.init();
        TagNameSet t = TagNameSet.of("john paul george ringo");
        String json = Json.toJson(t);
//        System.out.println(json);
        
        TagNameSet t2 = Json.fromJson(json, TagNameSet.class);
        assertEquals(4, t2.asSet().size());
        assertTrue(t2.contains(TagName.of("john")));
        assertTrue(t2.contains(TagName.of("paul")));
        assertTrue(t2.contains(TagName.of("george")));
        assertTrue(t2.contains(TagName.of("ringo")));
        assertFalse(t2.contains(TagName.of("pete")));
    }

    @Test
    public void testJsonEmptyRoundTrip() {
        JsonConfig.init();
        TagNameSet t = TagNameSet.of("    ");
        assertTrue(t.isEmpty());

        Container c = new Container();
        c.tags = t;
        String json = Json.toJson(c);
//        System.out.println(json);
        
        Container c2 = Json.fromJson(json, Container.class);
        TagNameSet t2 = c2.tags;
        assertNull(t2);
    }

    private static class Container {
        TagNameSet tags;
    }
}
