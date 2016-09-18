package com.martiansoftware.bookmartian.model;

import com.martiansoftware.bookmartian.model.Query.QueryTerm;
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
public class QueryTest {
    
    public QueryTest() {
    }
    
    @Test
    public void testGoodQueryTerms() {
        QueryTerm qt = QueryTerm.of("hello");
        assertEquals("tagged", qt.action());
        assertEquals("hello", qt.arg());
        
        qt = QueryTerm.of("tagged:Hello");
        assertEquals("tagged", qt.action());
        assertEquals("Hello", qt.arg());
        
        qt = QueryTerm.of("ABC:>def");
        assertEquals("abc", qt.action());
        assertEquals(">def", qt.arg());
    }
    
    private void expectQueryTermFailure(String rawQueryTerm) {
        try {
            QueryTerm qt = QueryTerm.of(rawQueryTerm);
            fail(String.format("accepted bad query term [%s]", rawQueryTerm));
        } catch (RuntimeException expected) {}
    }
    
    @Test
    public void testBadQueryTerms() {
        expectQueryTermFailure(":something");
        expectQueryTermFailure(":");
        expectQueryTermFailure("::");
        expectQueryTermFailure("a::");
        expectQueryTermFailure("a::b");
        expectQueryTermFailure("::b");
    }
    
    public void testNamedQuery() {
        Query q = Query.of("is:tagged as:my-tagged-stuff");
    }
}
