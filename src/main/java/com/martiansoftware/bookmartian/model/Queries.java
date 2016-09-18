package com.martiansoftware.bookmartian.model;

import com.martiansoftware.bookmartian.model.Query.QueryTerm;
import com.martiansoftware.util.Strings;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import static com.martiansoftware.util.Oops.oops;

/**
 *
 * @author mlamb
 */
public class Queries {

    // regex used to parse strings that might start with a comparison operator
    // result is two match groups: "op" with the operator and "arg" with the rest
    private static final Pattern COMPARISON_SPLITTER = Pattern.compile("^(?<op>(==|=|<=|<|>=|>))?(?<arg>.+)$");
    
    /**
     * Given a QueryTerm, convert it into a single Function for processing
     * a Stream of Bookmarks
     * @param qt the QueryTerm to convert
     * @return a Function that implements the logic specified by the QueryTerm
     */
    public static Function<Stream<Bookmark>, Stream<Bookmark>> of(QueryTerm qt) {
        switch(qt.action()) {
            // TODO: register actions via a map of action names to factory methods; this will support action aliases trivially
            
            case "is": return isQuery(Strings.lower(qt.arg()));

            case "tagged": return s -> s.filter(b -> b.tagNames().contains(TagName.of(qt.arg())));
            
            case "site": return siteQuery(qt.arg());
            
            case "visit-count": return visitCountQuery(qt.arg());
            
            case "limit": return s -> s.limit(Strings.asLong(qt.arg()));
            
            case "by": return byQuery(Strings.lower(qt.arg()));
            
            case "created": return dateQuery(b -> b.created(), qt);
            case "last-visited": return dateQuery(b -> b.lastVisited(), qt);
            case "last-modified": return dateQuery(b -> b.modified(), qt);
            
            default: return oops("invalid query action '%s'", qt.action());
        }
    }
    
    private static Function<Stream<Bookmark>, Stream<Bookmark>> isQuery(String siteArg) {
        switch(siteArg) {
            case "untagged": return s -> s.filter(b -> b.tagNames().isEmpty());
            case "tagged": return s -> s.filter(b -> !b.tagNames().isEmpty());
            default: return oops("invalid argument for 'is' query: '%s'", siteArg);
        }
    }

    // Dates also contain time data.  When we compare dates (e.g. "give me all
    // Bookmarks created on 2016/09/17") it's convenient to drop the time data
    // from the Bookmark's creation timestamp to simplify the comparison.
    private static Date stripTime(Date d) {
        if (d == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * Generates a Function implementing Date-specific logic
     * @param dateSource accessor method for obtaining the Date of interest from a Bookmark
     * @param qt
     */
    private static Function<Stream<Bookmark>, Stream<Bookmark>> dateQuery(Function<Bookmark, Optional<Date>> dateSource, QueryTerm qt) {
        Matcher m = COMPARISON_SPLITTER.matcher(qt.arg());
        if (!m.matches()) return oops("invalid %s param '%s'", qt.action(), qt.arg());
        BiPredicate<Date, Date> cmp = comparisonFunction(m.group("op"));        
        try {
            SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");
            Date d = DATE_FORMAT.parse(m.group("arg"));
            return s -> s.filter(b -> cmp.test(stripTime(dateSource.apply(b).orElse(null)), d));
        } catch (ParseException p) {
            return oops("invalid %s param '%s' - use yyyy/mm/dd format.", qt.action(), m.group("arg"));
        }
    }
    
    private static Function<Stream<Bookmark>, Stream<Bookmark>> siteQuery(String siteArg) {
        String siteName = Strings.lower(siteArg);        
        return s -> s.filter(
            b -> {
                try {
                    URL url = new URL(b.lurl().toString());
                    String host = Strings.lower(url.getHost());
                    return host.equals(siteName) || host.endsWith("." + siteName);
                } catch (MalformedURLException e) {
                    return false;
                }
            }
        );
    }

    private static Function<Stream<Bookmark>, Stream<Bookmark>> byQuery(String sortBy) {
        switch(sortBy) {
            case "most-recently-created": return s -> s.sorted(Bookmark.MOST_RECENTLY_CREATED_FIRST);
            case "least-recently-created": return s -> s.sorted(Bookmark.MOST_RECENTLY_CREATED_FIRST.reversed());
            
            case "most-recently-visited": return s -> s.sorted(Bookmark.MOST_RECENTLY_VISITED_FIRST);
            case "least-recently-visited": return s -> s.sorted(Bookmark.MOST_RECENTLY_VISITED_FIRST.reversed());

            case "most-recently-modified": return s -> s.sorted(Bookmark.MOST_RECENTLY_MODIFIED_FIRST);
            case "least-recently-modified": return s -> s.sorted(Bookmark.MOST_RECENTLY_MODIFIED_FIRST.reversed());

            case "most-visited": return s -> s.sorted(Bookmark.MOST_VISITED_FIRST);
            case "least-visited": return s -> s.sorted(Bookmark.MOST_VISITED_FIRST.reversed());
            
            case "title": return s -> s.sorted(Bookmark.BY_TITLE);
            case "url": return s -> s.sorted(Bookmark.BY_URL);
            
            default: return oops("invalid sort order '%s'", sortBy);
        }
    }
    
    private static Function<Stream<Bookmark>, Stream<Bookmark>> visitCountQuery(String vcParam) {
        Matcher m = COMPARISON_SPLITTER.matcher(vcParam);
        if (!m.matches()) return oops("invalid visit-count param '%s'", vcParam);
        BiPredicate<Long, Long> cmp = comparisonFunction(m.group("op"));
        return s -> s.filter(b -> cmp.test(b.visitCount().orElse(null), Strings.asLong(m.group("arg"))));
    }
    
    /**
     * Given an operator OP in the set "=", "==", "<", "<=", ">", ">=", returns a
     * BiPredicate that, given (a, b) returns a boolean indicating if "a OP b" is true.
     * 
     * A null or empty string is treated as "=="
     * 
     * This is used to filter results against user-specified expressions, like
     * "visit-count:>20" or "created:2016/01/01".  If the field of interest is
     * absent then the BiPredicate will return false.
     */
    private static <A extends Comparable<A>> BiPredicate<A, A> comparisonFunction(String op) {
        if (op == null) return comparisonFunction("==");
        switch(op) {
            case "":  // fall through
            case "=": // fall through
            case "==": return (a, b)-> a != null && b != null && a.compareTo(b) == 0;            
            case "<": return (a, b)-> a != null && b != null && a.compareTo(b) < 0;
            case "<=": return (a, b)-> a != null && b != null && a.compareTo(b) <= 0;
            case ">": return (a, b)-> a != null && b != null && a.compareTo(b) > 0;
            case ">=": return (a, b)-> a != null && b != null && a.compareTo(b) >= 0;
            default: return oops("invalid comparison operator '%s'", op);
        }
    }
}
