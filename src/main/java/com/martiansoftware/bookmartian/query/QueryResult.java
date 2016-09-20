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
 *
 * @author mlamb
 */
    // the type returned by Query.execute()
class QueryResult {
    private final String _name, _query, _sort;
    private final List<Bookmark> _bookmarks;
    private final Date _executed = new Date();

    QueryResult(String name, String query, String sort, List<Bookmark> bookmarks) {
        _name = name;
        _query = query;
        _sort = sort;
        _bookmarks = bookmarks;
    }
}

