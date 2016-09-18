package com.martiansoftware.bookmartian.model;

import com.martiansoftware.bookmartian.jsondir.JsonDirBookmartian;
import com.martiansoftware.boom.Json;
import com.martiansoftware.util.Strings;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static com.martiansoftware.util.Oops.oops;
import java.util.Date;

/**
 * A Bookmark query, created from a user-specified String.
 * 
 * See doc/queries.md in the source tree for a description of the query syntax.
 * 
 * @author mlamb
 */
public class Query {

    private final String _raw;
    private String _name;
    
    // the query is compiled into a single function that is simply run against
    // the full bookmark collection
    private Function<Stream<Bookmark>, Stream<Bookmark>> _compiledQuery;
    
    private Query(String rawQuery) {
        _raw = Strings.safeTrim(rawQuery);
        _name = _raw;
        
        _compiledQuery = compile(
                            Strings.splitOnWhitespaceAndCommas(rawQuery)
                            .stream()
                            .map(q -> QueryTerm.of(q))
                            .flatMap(qt -> maybeProcessGlobalQueryTerm(qt))
                            .collect(Collectors.toCollection(Stack::new)));                
    }
    
    private Stream<QueryTerm> maybeProcessGlobalQueryTerm(QueryTerm qt) {
        if ("as".equals(qt.action())) {
            _name = qt.arg();
            return Stream.empty();
        } else return Stream.of(qt);
    }
    
    private Function<Stream<Bookmark>, Stream<Bookmark>> compile(Stack<QueryTerm> queryTerms) {
        if (queryTerms.isEmpty()) return s -> s;
        return s -> Queries.of(queryTerms.pop()).apply(compile(queryTerms).apply(s));
    }

    public Result execute(IBookmartian bm) {
        return new Result(_name,
                            _raw,
                            Collections.unmodifiableList(
                                _compiledQuery.apply(
                                    bm.bookmarks()
                                        .stream()
                                ).collect(Collectors.toList())));
    }
    
    public static Query of(String rawQuery) {
        return new Query(rawQuery);
    }
    
    
    static class QueryTerm {
        private static final Pattern QUERYTERM_SPLITTER = Pattern.compile("^((?<action>[^:]+):)?(?<arg>[^:]+)$");
        private final String _action, _arg;
        private QueryTerm(String action, String argument) {
            _action = Strings.lower(Optional.ofNullable(Strings.safeTrimToNull(action)).orElse("tagged"));
            _arg = Strings.safeTrimToNull(argument);
        }
        static QueryTerm of(String rawQueryTerm) {
            Matcher m = QUERYTERM_SPLITTER.matcher(rawQueryTerm);
            if (!m.matches()) return oops("invalid query term '%s'", rawQueryTerm);
            return new QueryTerm(m.group("action"), m.group("arg"));
        }
        public String action() { return _action; }
        public String arg() { return _arg; }
    }
            
    public static class Result {
        private final String _name, _query;
        private final List<Bookmark> _bookmarks;
        private final Date _executed = new Date();
        
        Result(String name, String query, List<Bookmark> bookmarks) {
            _name = name;
            _query = query;
            _bookmarks = bookmarks;
        }
    }
    
    public static void main(String[] args) throws Exception {
//        Query2 qt2 = Query2.of("programming java created:7d by:most-recently-visited");
        JsonConfig.init(); // TODO: move this to JsonDirBookmartian, etc.
        IBookmartian bm = JsonDirBookmartian.in(Paths.get("/home/mlamb/bookmartian"));
        
        Query qt2 = Query.of("by:least-recently-created created:<2016/09/13 visit-count:1 as:my-test");
        System.out.println(Json.toJson(qt2.execute(bm)));
    }
    
}
