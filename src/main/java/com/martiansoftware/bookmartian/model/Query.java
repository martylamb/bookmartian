package com.martiansoftware.bookmartian.model;

import com.martiansoftware.util.Strings;
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
    
    // the vast majority of query terms are processed left-to-right and are
    // used to filter or organize the results.  it is possible, however, to
    // have query terms that do something else to the query and don't filter
    // results at all.  right now, the only one of these is the "as:query-name"
    // term which names the query.
    //
    // this method also provides a hook to remove or insert queryterms in place
    // in case that's ever useful for something else.
    private Stream<QueryTerm> maybeProcessGlobalQueryTerm(QueryTerm qt) {
        if ("as".equals(qt.action())) {
            _name = qt.arg();
            return Stream.empty();   // remove this query term; its job is done
        } else return Stream.of(qt); // leave query term as-is
    }
    
    // compiles a stack of individual QueryTerms into a single function that
    // can be evaluated against the full set of bookmarks.  query terms are
    // initially all pushed onto a stack, then composed in reverse order to
    // provide left-to-right evaluation.
    private Function<Stream<Bookmark>, Stream<Bookmark>> compile(Stack<QueryTerm> queryTerms) {
        if (queryTerms.isEmpty()) return s -> s;
        return s -> Queries.of(queryTerms.pop()).apply(compile(queryTerms).apply(s));
    }

    /**
     * Executes this Query against a bookmartian
     * @param bm the bookmartian to query
     * @return the results of the query, or all bookmarks if the query is empty
     */
    public Result execute(IBookmartian bm) {
        return new Result(_name,
                            _raw,
                            Collections.unmodifiableList(
                                _compiledQuery.apply(
                                    bm.bookmarks()
                                        .stream()
                                ).collect(Collectors.toList())));
    }

    /**
     * Creates a new Query based on a user-specified "raw" query string
     */
    public static Query of(String rawQuery) {
        return new Query(rawQuery);
    }
    
    // an individual "query term", consisting of an optional "action:" and an "argument"
    // for example, "is:untagged" has an action of "is" and an argument of "untagged"
    // if no action is specified, a default of "tagged" is used.
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
            
    // the type returned by Query.execute()
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
        
}
