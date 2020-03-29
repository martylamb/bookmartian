package com.martiansoftware.bookmartian.query;

import com.martiansoftware.bookmartian.model.Bookmark;
import com.martiansoftware.bookmartian.model.Bookmartian;
import com.martiansoftware.util.Strings;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Bookmark query, created from a user-specified String.
 * 
 * See doc/queries.md in the source tree for a description of the query syntax.
 * 
 * @author mlamb
 */
public class Query {

    private static final Logger log = LoggerFactory.getLogger(Query.class);
    private static final QueryFunction DEFAULT_SORT = Queries.of(QueryTerm.of("by", "title", false));
    
    // the raw query as specified by the user
    private final String _raw;
    
    // the query is compiled into a single function that is simply run against
    // the full bookmark collection
    private QueryFunction _compiledQuery;
    
    private Query(String rawQuery) {
        _raw = Strings.safeTrim(rawQuery);
        
        _compiledQuery = compile(new QueryTermParser()
                                        .parse(rawQuery)
                                        .stream()
                                        .collect(Collectors.toCollection(Stack::new))
                                );
    }
    
    // compiles a stack of individual QueryTerms into a single function that
    // can be evaluated against the full set of bookmarks.  query terms are
    // initially all pushed onto a stack, then composed in reverse order to
    // provide left-to-right evaluation.
    private QueryFunction compile(Stack<QueryTerm> queryTerms) {
        if (queryTerms.isEmpty()) return (s, r) -> s;
        return (s, r) -> Queries.of(queryTerms.pop()).apply(compile(queryTerms).apply(s, r), r);
    }

    private List<Bookmark> eval(QueryFunction qf, Collection<Bookmark> bookmarks, QueryResult qr) {
        return Collections.unmodifiableList(qf.apply(bookmarks.stream(), qr).collect(Collectors.toList()));
    }
    
    /**
     * Executes this Query against a bookmartian
     * @param bm the bookmartian to query
     * @return the results of the query, or all bookmarks if the query is empty
     */
    public QueryResult execute(Bookmartian bm) {
        QueryResult qr = new QueryResult().name(_raw);
        Collection<Bookmark> allBookmarks = bm.bookmarks();
        log.info("Executing query against {} bookmarks", allBookmarks.size());
        List<Bookmark> bookmarks = eval(_compiledQuery, allBookmarks, qr);        
        if (!qr.hasSort()) bookmarks = eval(DEFAULT_SORT, bookmarks, qr);
        
        return qr.bookmarks(bookmarks);
    }

    /**
     * Creates a new Query based on a user-specified "raw" query string
     * @param rawQuery the raw query string as provided by the user
     * @return a Query that is ready to execute
     */
    public static Query of(String rawQuery) {
        return new Query(rawQuery);
    }
        
}
