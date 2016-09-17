package com.martiansoftware.bookmartian.model;

import com.martiansoftware.util.Strings;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author mlamb
 */
public class Query implements Predicate<Bookmark> {
    
    private static final Pattern QUERY_PATTERN = Pattern.compile("^(?<type>[^:]+):(?<arg>[^:]+)$");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");
    private static final BiFunction<Date, Date, Boolean> DATE_BEFORE = (d1, d2) -> d1.compareTo(d2) < 0;
    private static final BiFunction<Date, Date, Boolean> DATE_AFTER = (d1, d2) -> d1.compareTo(d2) > 0;
    
    private final Predicate<Bookmark> _delegate;
    
    private Query(Set<String> query) {
        Set<String> tags = new java.util.HashSet<>();
        List<Predicate<Bookmark>> delegates = new java.util.ArrayList<>();

        for (String s : query) {
            if (s.contains(":")) {
                delegates.add(mkPredicate(s));
            } else {
                tags.add(s);
            }
        }
        if (!tags.isEmpty()) delegates.add(b -> b.tagNames().containsAll(TagNameSet.of(tags)));

        Predicate<Bookmark> delegate = null;
        for (Predicate<Bookmark> d : delegates) {
            delegate = (delegate == null) ? d : d.and(delegate);
        }
        _delegate = (delegate == null) ? b -> true : delegate;
    }
    
    public static Query of(Set<String> query) {
        return new Query(query);
    }

    @Override
    public boolean test(Bookmark b) {
        return _delegate.test(b);
    }

    private Date getDate(String query, String queryArg) {
        try {
            return DATE_FORMAT.parse(queryArg);
        } catch (Exception e) {
            badQuery(query, "invalid date \"%s\" - expected format is YYYY/MM/DD", queryArg);
            return null; // not executed; badQuery throws a runtimeexception
        }
    }
    
    
    private Predicate<Bookmark> datePredicate(Function<Bookmark,Optional<Date>> dateSource, Date comparisonFunction, BiFunction<Date, Date, Boolean> dateComparison) {
        return b -> {
            Optional<Date> od = dateSource.apply(b);
            if (od == null || !od.isPresent()) return false;
            return dateComparison.apply(od.get(), comparisonFunction);
        };
    }
    
    private Date oneWeekAgo() {
        return new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 7));
    }
    
    private Predicate<Bookmark> mkPredicate(String s) {
        Matcher m = QUERY_PATTERN.matcher(s);
        if (!m.matches()) badQuery(s, "syntax error");
        
        String type = Strings.lower(m.group("type"));
        String arg = m.group("arg");
        Date d;
        switch(type) {
            case "is":
                arg = Strings.lower(arg);
                if ("untagged".equals(arg)) return b -> b.tagNames().isEmpty();
                if ("recent".equals(arg)) return datePredicate(b -> b.created(), oneWeekAgo(), DATE_AFTER);
                badQuery(s, "unrecognized argument \"%s\".  valid arguments are \"untagged\" and \"recent\"", arg);
    
            case "created-before":
                return datePredicate(b -> b.created(), getDate(s, arg), DATE_BEFORE);
            
            case "created-after":
                return datePredicate(b -> b.created(), getDate(s, arg), DATE_AFTER);

            case "last-visited-before":
                return datePredicate(b -> b.lastVisited(), getDate(s, arg), DATE_BEFORE);

            case "last-visited-after":
                return datePredicate(b -> b.lastVisited(), getDate(s, arg), DATE_AFTER);

            case "site":
                String siteName = Strings.lower(arg);
                return b -> {
                    try {
                        URL url = new URL(b.lurl().toString());
                        String host = Strings.lower(url.getHost());
                        return host.equals(siteName) || host.endsWith("." + siteName);
                    } catch (MalformedURLException e) {
                        return false;
                    }
                };
                
            default:
                badQuery(s, "unrecognized query type \"%s\"", arg);
        }
        return null; // never called; badQuery throws an exception
    }

    private void badQuery(String q, String fmt, Object... o) {
        throw new IllegalArgumentException(
                    String.format("error in query \"%s\": %s",
                        q,
                        String.format(fmt, o)));
    }
}
