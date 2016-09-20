package com.martiansoftware.bookmartian.model;

import com.martiansoftware.bookmartian.model.QueryTermParser.QueryTermParseException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mlamb
 */
public class QueryTermParserTest {    

    @Test
    public void testGoodQueries() {
        QueryTermParser q = new QueryTermParser();
        q.parse("a b by:most-recently-visited");
        q.parse("");
        q.parse("     \t\n                    ");
        q.parse("hello");
        q.parse("something:simple");
        q.parse("something:\"quoted\"");
        q.parse("something:\"quoted\" and other stuff");
        q.parse("a:\"quoted and \"\"escaped\"\" string!\" def");
        q.parse("and, oh yeah, we support:commas, too");
        q.parse("and-also:actions with-dashes");
    }

    private void failAt(int pos, String s) {
        QueryTermParser q = new QueryTermParser();
        try {
            q.parse(s);
            fail("Expected failed parse of [" + s + "] at position " + pos + ", but parse passed.");
        } catch (QueryTermParseException e) {
            assertEquals(pos, e.position());
        }
    }
    
    @Test
    public void testBadQueries() {
        failAt(0, ":hello"); // no action
        failAt(0, "\"quotes"); // quoted action
        failAt(3, "   *asd"); // invalid action char
        failAt(3, "abc&d"); // invalid action char
        failAt(22, "an:\"unterminated quote"); // unterminated quote
        failAt(9, "a:\"quote\"withjunkafter"); // no whitespace after close quote
        failAt(5, "  abd\":de"); // quote in action
    }
    
}
