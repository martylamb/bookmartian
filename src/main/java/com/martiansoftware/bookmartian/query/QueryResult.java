/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.martiansoftware.bookmartian.query;

import com.martiansoftware.bookmartian.model.Bookmark;
import java.util.Date;
import java.util.List;

/**
 * a MUTABLE class for storing/returning query results and metadata.
 * 
 * @author mlamb
 */
    // the type returned by Query.execute()
class QueryResult {
    private String _name, _query, _sort;
    private List<Bookmark> _bookmarks;
    private final Date _executed = new Date();
    private long _elapsedTimeMs;
    private transient long _started = System.currentTimeMillis();

    public QueryResult name(String name) { _name = name; return this; }
    public QueryResult query(String query) { _query = query; return this;}
    public boolean hasSort() { return _sort != null; }
    public QueryResult sort(String sort) { _sort = sort; return this;}
    
    public QueryResult bookmarks(List<Bookmark> bookmarks) {
        _bookmarks = bookmarks;
        _elapsedTimeMs = System.currentTimeMillis() - _started;
        return this;
    }
}

