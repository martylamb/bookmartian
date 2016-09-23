package com.martiansoftware.bookmartian.query;

import com.martiansoftware.bookmartian.model.Bookmark;
import java.util.stream.Stream;

/**
 *
 * @author mlamb
 */
@FunctionalInterface
public interface QueryFunction {
    Stream<Bookmark> apply(Stream<Bookmark> bookmarks, QueryResult result);
}
