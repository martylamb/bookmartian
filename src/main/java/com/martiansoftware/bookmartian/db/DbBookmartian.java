package com.martiansoftware.bookmartian.db;

import com.martiansoftware.bookmartian.model.Bookmark;
import com.martiansoftware.bookmartian.model.Bookmartian;
import com.martiansoftware.bookmartian.model.Color;
import com.martiansoftware.bookmartian.model.Lurl;
import com.martiansoftware.bookmartian.model.Tag;
import com.martiansoftware.bookmartian.model.TagName;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.martiansoftware.bookmartian.model.User;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author mlamb
 */
class DbBookmartian implements Bookmartian {
  
    private static final Logger log = LoggerFactory.getLogger(DbBookmartian.class);

    private final Database _db;
    private final String _userName;
    
    DbBookmartian(Database db, User user) {
        _db = db;
        _userName = user.username();
    }

    @Override
    public Collection<Bookmark> bookmarks() {
        return _db.call(conn -> loadAllBookmarks(conn));
    }
    
    @Override
    public Optional<Bookmark> get(Lurl lurl) {
        return _db.call(conn -> loadBookmarkByLurl(conn, lurl));
    }
    
    @Override
    public Optional<Bookmark> remove(Lurl lurl) {
        return _db.call(conn -> {
            Optional<Bookmark> old = loadBookmarkByLurl(conn, lurl);
            deleteBookmarkByLurl(conn, lurl);
            return old;
        });
    }
    
    @Override
    public Optional<Bookmark> visit(Lurl lurl) {
        return _db.call(conn -> {
            Optional<Bookmark> b = loadBookmarkByLurl(conn, lurl);
            if (!b.isPresent()) return b;
            return Optional.of(saveBookmark(conn, b.get().toBuilder()
                                                    .lastVisited(new Date())
                                                    .visitCount(b.get().visitCount().map(x -> x + 1).orElse(1l))
                                                    .build()));
        });        
    }
    
    private <T> Stream<T> mergeStream(Function<Bookmark, T> f, Optional<Bookmark>... toMerge) {
        return Stream.of(toMerge)
                .filter(tm -> tm.isPresent())
                .map(tm -> tm.get())
                .map(f::apply)
                .filter(d -> d != null);
    }
    
    @Override
    public Bookmark update(Lurl replacing, Bookmark bookmark) {
        return _db.call(conn -> {            
            Bookmark b = bookmark.merge(loadBookmarkByLurl(conn, bookmark.lurl()));
            if (replacing != null) {
                b = bookmark.merge(loadBookmarkByLurl(conn, replacing));
                if (!bookmark.lurl().equals(replacing)) deleteBookmarkByLurl(conn, replacing);
            }
            return saveBookmark(conn, b);
        });
    }

    @Override
    public Collection<Tag> tags() {
        return _db.call(conn -> loadAllTags(conn));
    }
    
    @Override
    public Optional<Tag> get(TagName tn) {
        return _db.call(conn -> loadTagByTagName(conn, tn));
    }

// ----------------------------------------------------------------------------
        
    private Bookmark loadBookmark(ResultSet rs) throws SQLException {
        Bookmark result =  Bookmark.newBuilder()
                            .url(rs.getString("URL"))
                            .title(rs.getString("TITLE"))
                            .notes(rs.getString("NOTES"))
                            .imageUrl(rs.getString("IMAGEURL"))
                            .tags(rs.getString("TAGS"))
                            .created(rs.getTimestamp("CREATED"))
                            .modified(rs.getTimestamp("MODIFIED"))
                            .lastVisited(rs.getTimestamp("LASTVISITED"))
                            .visitCount(rs.getLong("VISITCOUNT"))
                            .build();
        log.info("loaded bookmark {}", result.lurl());
        return result;
    }

    private Timestamp ts(Date d) { return new Timestamp(d.getTime()); }
    
    private Timestamp now() { return ts(new Date()); }
    
    private Bookmark saveBookmark(Connection conn, Bookmark bm) throws SQLException {
        try (PreparedStatement u = conn.prepareStatement("MERGE INTO BOOKMARKS "
                                    //  1      2         3      4       5        6       7       8          9           10
                                    + "(URL, USERNAME, TITLE, NOTES, IMAGEURL, TAGS, CREATED, MODIFIED, LASTVISITED, VISITCOUNT) "
                                    + "VALUES (?,?,?,?,?,?,?,?,?,?);")) {
            Timestamp now = now();
            u.setString(1, bm.lurl().toString());
            u.setString(2, _userName);
            u.setString(3, bm.title().orElse(null));
            u.setString(4, bm.notes().orElse(null));
            u.setString(5, bm.imageUrl().orElse(null));
            u.setString(6, bm.tagNames().asSet().stream().map(t -> t.toString()).collect(Collectors.joining(" ")));
            u.setTimestamp(7, bm.created().map(d -> ts(d)).orElse(now));
            u.setTimestamp(8, bm.modified().map(d -> ts(d)).orElse(now));
            u.setTimestamp(9, bm.lastVisited().map(d -> ts(d)).orElse(null));
            u.setLong(10, bm.visitCount().orElse(null));
            u.executeUpdate();
        }
        // TODO: make sure tags exist
        return bm;
    }

    private Collection<Bookmark> loadAllBookmarks(Connection conn) throws SQLException {
        Collection<Bookmark> results = new java.util.HashSet<>();//TreeSet<>(Bookmark.MOST_RECENTLY_CREATED_FIRST); 
        try (PreparedStatement q = conn.prepareStatement("SELECT * FROM BOOKMARKS WHERE USERNAME = ?;")) {
            q.setString(1, _userName);
            ResultSet rs = q.executeQuery();
            while (rs.next()) results.add(loadBookmark(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        log.info("loaded {} bookmarks", results.size());
        return Collections.unmodifiableCollection(results);
    }

    private Optional<Bookmark> loadBookmarkByLurl(Connection conn, Lurl lurl) throws SQLException {
        try (PreparedStatement q = conn.prepareStatement("SELECT * FROM BOOKMARKS WHERE USERNAME = ? AND URL = ?;")) {
            q.setString(1, _userName);
            q.setString(2, lurl.toString());
            ResultSet rs = q.executeQuery();
            if (rs.first()) return Optional.ofNullable(loadBookmark(rs));
        }        
        return Optional.empty();
    }
    
    private void deleteBookmarkByLurl(Connection conn, Lurl lurl) throws SQLException {
        try (PreparedStatement u = conn.prepareStatement("DELETE FROM BOOKMARKS WHERE USERNAME = ? AND URL = ?;")) {
            u.setString(1, _userName);
            u.setString(2, lurl.toString());
            u.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }    
        // TODO: delete tags that are no longer in use?
    }

// ----------------------------------------------------------------------------
        
    private Tag loadTag(ResultSet rs) throws SQLException {
        return Tag.newBuilder()
                .name(rs.getString("TAGNAME"))
                .color(Color.of(rs.getString("COLOR")))
                .build();
    }

    private Collection<Tag> loadAllTags(Connection conn) throws SQLException {
        Collection<Tag> results = new java.util.TreeSet<>(); 
        try (PreparedStatement q = conn.prepareStatement("SELECT * FROM TAGS WHERE USERNAME = ?;")) {
            q.setString(1, _userName);
            ResultSet rs = q.executeQuery();
            while (rs.next()) results.add(loadTag(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Collections.unmodifiableCollection(results);
    }

    private Optional<Tag> loadTagByTagName(Connection conn, TagName tn) throws SQLException {
        try (PreparedStatement q = conn.prepareStatement("SELECT * FROM TAGS WHERE USERNAME = ? AND TAGNAME = ?;")) {
            q.setString(1, _userName);
            q.setString(2, tn.toString());
            ResultSet rs = q.executeQuery();
            if (rs.first()) return Optional.ofNullable(loadTag(rs));
        }        
        return Optional.empty();
    }
    
}
