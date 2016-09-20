package com.martiansoftware.bookmartian.query;

import com.martiansoftware.bookmartian.model.Bookmark;
import com.martiansoftware.bookmartian.model.IBookmartian;
import com.martiansoftware.util.Strings;
import java.util.Collections;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A Bookmark query, created from a user-specified String.
 * 
 * See doc/queries.md in the source tree for a description of the query syntax.
 * 
 * @author mlamb
 */
public class Query {

    private final String _raw;
    private String _name, _sort;
    
    // the query is compiled into a single function that is simply run against
    // the full bookmark collection
    private Function<Stream<Bookmark>, Stream<Bookmark>> _compiledQuery;
    
    private Query(String rawQuery) {
        _raw = Strings.safeTrim(rawQuery);
        _name = _raw;
        
        Stack<QueryTerm> queryStack = 
                            new QueryTermParser()
                            .parse(rawQuery)
                            .stream()
                            .flatMap(qt -> maybeProcessGlobalQueryTerm(qt))
                            .collect(Collectors.toCollection(Stack::new));
        
        // if no sort is specified, default to most-recently created
        _sort = Strings.lower(
                    queryStack.stream()                    
                    .filter(qt -> "by".equals(qt.action()))                            
                    .reduce((a, b) -> b) // essentially, "Stream.findLast()"      
                    .orElseGet(() -> queryStack.push(QueryTerm.of("by", "title")))
                    .toString());
                
        _compiledQuery = compile(queryStack);                
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
    public QueryResult execute(IBookmartian bm) {
        return new QueryResult(_name,
                            _raw,
                            _sort,
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
    


        
}
