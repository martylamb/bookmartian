package com.martiansoftware.bookmartian.model;

import com.martiansoftware.bookmartian.model.Query.QueryTerm;
import com.martiansoftware.util.Strings;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import static com.martiansoftware.util.Oops.oops;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author mlamb
 */
public class Queries {

    // regex used to parse strings that might start with a comparison operator
    // result is two match groups: "op" with the operator and "arg" with the rest
    private static final Pattern COMPARISON_SPLITTER = Pattern.compile("^(?<op>(==|=|<=|<|>=|>))?(?<arg>.+)$");
    
    // QTHandlers implement the handle() method to convert a QueryTerm into a
    // Bookmark stream filtering function.  If they don't handle the type of
    // QueryTerm passed, they return null.  If there's an error handling a
    // QueryTerm, they can throw an exception which will eventually go to the user.
    private interface QTHandler {
        public boolean handles(QueryTerm qt);
        public Function<Stream<Bookmark>, Stream<Bookmark>> handle(QueryTerm qt);
        public Stream<String> help();
    }
    
    private static final List<QTHandler> QT_HANDLERS;
    static {
        List<QTHandler> qtHandlers = new java.util.ArrayList<>();
        qtHandlers.add(new Is());
        qtHandlers.add(new Tagged());
        qtHandlers.add(new Site());
        qtHandlers.add(new Limit());
        qtHandlers.add(new By());
        qtHandlers.add(new Visits());
        qtHandlers.add(new Dates());
        QT_HANDLERS = Collections.unmodifiableList(qtHandlers);
    }
        
    /**
     * Given a QueryTerm, convert it into a single Function for processing
     * a Stream of Bookmarks
     * @param qt the QueryTerm to convert
     * @return a Function that implements the logic specified by the QueryTerm
     */
    public static Function<Stream<Bookmark>, Stream<Bookmark>> of(QueryTerm qt) {
        return QT_HANDLERS
                .stream()
                .filter(qth -> qth.handles(qt))
                .findFirst()
                .map(qth -> qth.handle(qt))
                .orElseGet(() -> oops("invalid query action '%s'", qt.action()));
    }

    private static class Is implements QTHandler {
        @Override public boolean handles(QueryTerm qt) { return "is".equals(qt.action()); }
        @Override public Function<Stream<Bookmark>, Stream<Bookmark>> handle(QueryTerm qt) {
            switch(Strings.lower(qt.arg())) {
                case "untagged": return s -> s.filter(b -> b.tagNames().isEmpty());
                case "tagged": return s -> s.filter(b -> !b.tagNames().isEmpty());
                case "secure": return s -> s.filter(b -> b.lurl().toString().startsWith("https://"));
                default: return oops("invalid argument for 'is' query: '%s'", qt.arg());
            }        
        }
        @Override public Stream<String> help() { return Stream.of("is:untagged", "is:tagged", "is:secure"); }
    }

    private static class Tagged implements QTHandler {
        @Override public boolean handles(QueryTerm qt) { return "tagged".equals(qt.action()); }
        @Override public Function<Stream<Bookmark>, Stream<Bookmark>> handle(QueryTerm qt) {
            return s -> s.filter(b -> b.tagNames().contains(TagName.of(qt.arg())));
        }
        @Override public Stream<String> help() { return Stream.of("[tagged:]TAG"); }        
    }
    
    private static class Site implements QTHandler {
        @Override public boolean handles(QueryTerm qt) { return "site".equals(qt.action()); }
        @Override public Function<Stream<Bookmark>, Stream<Bookmark>> handle(QueryTerm qt) {
            String siteName = Strings.lower(qt.arg());
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
        @Override public Stream<String> help() { return Stream.of("site:SITE"); }        
    }

    private static class Limit implements QTHandler {
        @Override public boolean handles(QueryTerm qt) { return "limit".equals(qt.action()); }
        @Override public Function<Stream<Bookmark>, Stream<Bookmark>> handle(QueryTerm qt) {
            return s -> s.limit(Strings.asLong(qt.arg()));
        }
        @Override public Stream<String> help() { return Stream.of("limit:N"); }        
    }

    private static class By implements QTHandler {
        @Override public boolean handles(QueryTerm qt) { return "by".equals(qt.action()); }
        @Override public Function<Stream<Bookmark>, Stream<Bookmark>> handle(QueryTerm qt) {
            switch(Strings.lower(qt.arg())) {
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
                default: return oops("invalid sort order '%s'", qt.arg());
            }
        }
        @Override public Stream<String> help() { 
            return Stream.concat(
                        Stream.of("recently-created", "recently-visited", "recently-modified", "visited")
                            .flatMap(s -> Stream.of("most-" + s, "least-" + s)),
                        Stream.of("title", "url")
            ).map(s -> "by:" + s);
        }        
    }

    private static class Visits implements QTHandler {
        private final Pattern p = Pattern.compile("^visits(?<op>-over|-under)?$");
        @Override public boolean handles(QueryTerm qt) { return p.matcher(qt.action()).matches(); }
        @Override public Function<Stream<Bookmark>, Stream<Bookmark>> handle(QueryTerm qt) {
            Matcher m = p.matcher(qt.action());
            if (!m.matches()) oops("bad visit query: %s", qt.toString());
            BiPredicate<Long, Long> cmp = comparisonFunction(m.group("op"));
            return s -> s.filter(b -> cmp.test(b.visitCount().orElse(null), Strings.asLong(qt.arg())));
        }
        @Override public Stream<String> help() { return Stream.of("visits:N", "visits-over:N", "visits-under:N"); }
    }
    
    private static class Dates implements QTHandler {
        private final Pattern p = Pattern.compile("^(?<field>created|modified|visited)(?<op>-on|-since|-before)?$");
        @Override public boolean handles(QueryTerm qt) { return p.matcher(qt.action()).matches(); }        
        @Override public Function<Stream<Bookmark>, Stream<Bookmark>> handle(QueryTerm qt) {
            Matcher m = p.matcher(qt.action());            
            if (!m.matches()) oops("bad date query: %s", qt.toString());
            BiPredicate<Date, Date> cmp = comparisonFunction(m.group("op"));
            return s -> s.filter(b -> cmp.test(
                                            fieldReader(m.group("field")).apply(b).orElse(null),
                                            com.martiansoftware.util.Dates.stripTime(dateOf(qt.arg()))
                                    )
                                );
        }
        private Date dateOf(String s) {
            try { return new SimpleDateFormat("yyyy/MM/dd").parse(s); } catch (Exception ignored) {}
            try { return RelativeDateParser.parse(s); } catch (Exception ignored) {}
            return oops("unable to interpret '%s' as a date", s);
        }
        private Function<Bookmark, Optional<Date>> fieldReader(String fieldName) {
            switch(fieldName) {
                case "created": return b -> b.created();
                case "modified": return b -> b.modified();
                case "visited": return b -> b.lastVisited();
                default: return oops("no such field: '%s'", fieldName);
            }
        }
        @Override public Stream<String> help() {
            return Stream.of("created", "modified", "visited")
                    .flatMap(s -> Stream.of(s + "[-on]", s + "-since", s + "-after"))
                    .map(s -> s + ":DATE-EXPR");
        }        
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
            case "-on":
            case "==": return (a, b)-> a != null && b != null && a.compareTo(b) == 0;
            case "-under":
            case "-before":
            case "<": return (a, b)-> a != null && b != null && a.compareTo(b) < 0;
            case "<=": return (a, b)-> a != null && b != null && a.compareTo(b) <= 0;
            case "-over":
            case ">": return (a, b)-> a != null && b != null && a.compareTo(b) > 0;
            case "-since":
            case ">=": return (a, b)-> a != null && b != null && a.compareTo(b) >= 0;
            default: return oops("invalid comparison operator '%s'", op);
        }
    }
    
    public static Stream<String> help() {
        return QT_HANDLERS.stream().flatMap(qth -> qth.help()).sorted();
    }
    
//    public static void main(String[] args) {
//        help().forEach(System.out::println);
//    }
}
