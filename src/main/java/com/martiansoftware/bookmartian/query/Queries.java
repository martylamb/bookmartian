package com.martiansoftware.bookmartian.query;

import com.martiansoftware.bookmartian.model.Bookmark;
import com.martiansoftware.bookmartian.model.TagName;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        public QueryFunction handle(QueryTerm qt);
    }
    
    // register all of the different query handlers
    private static final List<QTHandler> QT_HANDLERS;
    static {
        List<QTHandler> qtHandlers = new java.util.ArrayList<>();
        qtHandlers.add(new As());
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
    public static QueryFunction of(QueryTerm qt) {
        return QT_HANDLERS
                .stream()
                .filter(qth -> qth.handles(qt))
                .findFirst()
                .map(qth -> qth.handle(qt))
                .orElseGet(() -> oops("invalid query action '%s'", qt.action()));
    }

    private static class As implements QTHandler {
        @Override public boolean handles(QueryTerm qt) { return "as".equals(qt.action()); }
        @Override public QueryFunction handle(QueryTerm qt) {
            return (s, r) -> { r.name(qt.arg()); return s; };
        }
    }
    
    private static class Is implements QTHandler {
        @Override public boolean handles(QueryTerm qt) { return "is".equals(qt.action()); }
        @Override public QueryFunction handle(QueryTerm qt) {
            switch(Strings.lower(qt.arg())) {
                case "untagged": return (s, r) -> s.filter(b -> b.tagNames().isEmpty());
                case "tagged": return (s, r) -> s.filter(b -> !b.tagNames().isEmpty());
                case "secure": return (s, r) -> s.filter(b -> b.lurl().toString().startsWith("https://"));
                default: return oops("invalid argument for 'is' query: '%s'", qt.arg());
            }        
        }
    }

    private static class Tagged implements QTHandler {
        @Override public boolean handles(QueryTerm qt) { return "tagged".equals(qt.action()); }
        @Override public QueryFunction handle(QueryTerm qt) {
            return (s, r) -> s.filter(b -> b.tagNames().contains(TagName.of(qt.arg())));
        }
    }
    
    private static class Site implements QTHandler {
        @Override public boolean handles(QueryTerm qt) { return "site".equals(qt.action()); }
        @Override public QueryFunction handle(QueryTerm qt) {
            String siteName = Strings.lower(qt.arg());
            return (s, r) -> s.filter(
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
    }

    private static class Limit implements QTHandler {
        @Override public boolean handles(QueryTerm qt) { return "limit".equals(qt.action()); }
        @Override public QueryFunction handle(QueryTerm qt) {
            return (s, r) -> s.limit(Strings.asLong(qt.arg()));
        }
    }

    private static class By implements QTHandler {
        private static final Map<String, Comparator<Bookmark>> c;        
        static {
            c = new java.util.HashMap<>();
            c.put("most-recently-created", Bookmark.MOST_RECENTLY_CREATED_FIRST);
            c.put("least-recently-created", Bookmark.MOST_RECENTLY_CREATED_FIRST.reversed());
            c.put("most-recently-visited", Bookmark.MOST_RECENTLY_VISITED_FIRST);
            c.put("least-recently-visited", Bookmark.MOST_RECENTLY_VISITED_FIRST.reversed());
            c.put("most-recently-modified", Bookmark.MOST_RECENTLY_MODIFIED_FIRST);
            c.put("least-recently-modified", Bookmark.MOST_RECENTLY_MODIFIED_FIRST.reversed());
            c.put("most-visited", Bookmark.MOST_VISITED_FIRST);
            c.put("least-visited", Bookmark.MOST_VISITED_FIRST.reversed());
            c.put("title", Bookmark.BY_TITLE);
            c.put("url", Bookmark.BY_URL);
        }
        private Comparator<Bookmark> getComparator(String name) {            
            return c.getOrDefault(name, Bookmark.BY_TAGNAME(TagName.of(name)));
        }
        @Override public boolean handles(QueryTerm qt) { return "by".equals(qt.action()); }
        
        @Override public QueryFunction handle(QueryTerm qt) {
            List<String> sorts = Strings.splitOnWhitespaceAndCommas(Strings.lower(qt.arg()));
            Comparator<Bookmark> cmp = null;
            for (String sort : sorts) {
                cmp = (cmp == null) ? getComparator(sort) : cmp.thenComparing(getComparator(sort));
            }
            if (cmp == null) oops("no sort specified!");
            
            QueryTerm normalizedSort = QueryTerm.of("by", sorts.stream().collect(Collectors.joining(" ")));
            Comparator finalComparator = cmp;
            return(s, r) -> {
                r.sort(normalizedSort.toString());
                return s.sorted(finalComparator);
            };
        }
    }

    private static class Visits implements QTHandler {
        private static final String VISITS = "visits";
        private final Pattern p = Pattern.compile("^" + VISITS + "(?<op>" + COMPARISONS.numRegex() + ")?$");
        @Override public boolean handles(QueryTerm qt) { return p.matcher(qt.action()).matches(); }
        @Override public QueryFunction handle(QueryTerm qt) {
            Matcher m = p.matcher(qt.action());
            if (!m.matches()) oops("bad visit query: %s", qt.toString());
            BiPredicate<Long, Long> cmp = COMPARISONS.forNumSuffix(m.group("op")).asFunction();
            return (s, r) -> s.filter(b -> cmp.test(b.visitCount().orElse(null), Strings.asLong(qt.arg())));
        }
    }
    
    private static class Dates implements QTHandler {
        // helper enum for reading fields by name.  enum names are uppercase forms of what
        // the user may specify!
        private enum FIELDS {
            CREATED(b -> b.created()), MODIFIED(b -> b.modified()), VISITED(b -> b.lastVisited());
            private final Function<Bookmark, Optional<Date>> _fieldGetter;
            private FIELDS(Function<Bookmark, Optional<Date>> fieldGetter) { _fieldGetter = fieldGetter; }
            public Optional<Date> readFrom(Bookmark b) { return _fieldGetter.apply(b); }
            public static FIELDS forName(String s) {
                String fname = Strings.upper(s);
                for (FIELDS f : FIELDS.values()) if (fname.equals(f.name())) return f;
                return oops("no such field '%s'", s);
            }
        }
        
        // dynamically generate the pattern so we don't have to repeat ourselves (and thus screw something up)
        private final Pattern p = Pattern.compile("^(?<field>"
                                                    + Stream.of(FIELDS.values()).map(f -> Strings.lower(f.toString())).collect(Collectors.joining("|"))
                                                    + ")(?<op>"
                                                    + COMPARISONS.dateRegex()
                                                    + ")?$");
        
        @Override public boolean handles(QueryTerm qt) { return p.matcher(qt.action()).matches(); }        
        
        @Override public QueryFunction handle(QueryTerm qt) {
            Matcher m = p.matcher(qt.action());            
            if (!m.matches()) oops("bad date query: %s", qt.toString());
            BiPredicate<Date, Date> cmp = COMPARISONS.forDateSuffix(m.group("op")).asFunction();
            return (s, r) -> s.filter(b -> cmp.test(
                                            FIELDS.forName(m.group("field")).readFrom(b).orElse(null),
                                            com.martiansoftware.util.Dates.stripTime(dateOf(qt.arg()))
                                      )
                                 );
        }
        
        private Date dateOf(String s) {
            try { return new SimpleDateFormat("yyyy/MM/dd").parse(s); } catch (Exception ignored) {}
            try { return RelativeDateParser.parse(s); } catch (Exception ignored) {}
            return oops("unable to interpret '%s' as a date", s);
        }
    }
    
    /**
     * Given various suffixes for commands (e.g. "-since", "-under", ...), 
     * provides a way to obtain a BiPredicate that performs an appropriate
     * comparison of two values.
     */    
    private enum COMPARISONS {
        GTE, LT, EQ;
        
        // maps query action suffixes to specific comparisons for dates
        private static final Map<String, COMPARISONS> DATEMAP = new java.util.HashMap<>();

        // maps query action suffixes to specific comparisons for numbers
        private static final Map<String, COMPARISONS> NUMMAP = new java.util.HashMap<>();
        
        static {
            DATEMAP.put("[-on]", EQ); DATEMAP.put(null, EQ); DATEMAP.put("", EQ); DATEMAP.put("-before", LT); DATEMAP.put("-since", GTE);
            NUMMAP.put(null, EQ); NUMMAP.put("", EQ); NUMMAP.put("-under", LT); NUMMAP.put("-over", GTE);
        }
        
        // takes the keys of the specified map and converts them into a regex of the form a|b|c
        private static String regex(Map<String, COMPARISONS> map) {            
            return map.keySet().stream()
                    .filter(s -> !Strings.isEmpty(s))
                    .map(s -> s.replace("[", "").replace("]", "")) // square brackes are there for help usage.  drop them for the regex.
                    .collect(Collectors.joining("|")); 
        }
        
        // regex for the various date suffixes
        public static String dateRegex() { return regex(DATEMAP); }
        
        // regex for the various number suffixes
        public static String numRegex() { return regex(NUMMAP); }
        
        public <A extends Comparable<A>> BiPredicate<A, A> asFunction() {
            switch(this) {
                case GTE: return (a, b)-> a != null && b != null && a.compareTo(b) >= 0;
                case LT: return (a, b)-> a != null && b != null && a.compareTo(b) < 0;
                case EQ: return (a, b)-> a != null && b != null && a.compareTo(b) == 0;
                default: throw new IllegalStateException("invalid comparison");
            }
        }
        
        private static COMPARISONS forSuffix(Map<String, COMPARISONS> map, String suffix) {
            return Optional.ofNullable(map.get(Strings.lower(Strings.safeTrimToNull(suffix)))).orElseGet(() -> oops("no comparison for suffix '%s'", suffix));
        }
        public static COMPARISONS forDateSuffix(String suffix) { return forSuffix(DATEMAP, suffix); }
        public static COMPARISONS forNumSuffix(String suffix) { return forSuffix(NUMMAP, suffix); }
    }
    
}
