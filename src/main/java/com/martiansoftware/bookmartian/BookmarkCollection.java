package com.martiansoftware.bookmartian;

import com.martiansoftware.bookmartian.model.Bookmark;
import com.martiansoftware.bookmartian.model.TagNameSet;
import com.martiansoftware.bookmartian.model.Tag;
import com.martiansoftware.boom.Json;
import com.martiansoftware.util.Strings;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mlamb
 */
public class BookmarkCollection {
//
//    private static final Logger log = LoggerFactory.getLogger(BookmarkCollection.class);
//
//    private final transient Path _file;
//    private final transient Object _lock = new Object();
//    
//    private final transient Map<String, Bookmark> _bookmarksByUrl;
//    private final TreeSet<Bookmark> _bookmarks;
//    private final transient Map<String, Tag> _tagsByNormalizedName;
//    
//    private final TreeSet<Tag> _tags;    
//    
//    public BookmarkCollection(Path file) throws IOException {
//        _file = file.toAbsolutePath();
//        log.info("using bookmarks file {}", _file);
//        if (Files.isDirectory(_file)) throw new IOException(String.format("%s is a directory", _file));
//        _bookmarksByUrl = new java.util.HashMap<>();
//        _tagsByNormalizedName = new java.util.HashMap<>();
//        
//        if (Files.exists(_file)) {
//            log.info("loading bookmarks...");
//            SavedCollection sc = Json.fromJson(_file, SavedCollection.class);
//            _bookmarks = sc.bookmarks;
//            _tags = sc.tags;
//            _bookmarks.stream().forEach(b -> _bookmarksByUrl.put(b.url(), b));
//            _tags.stream().forEach(t -> _tagsByNormalizedName.put(t.normalizedName(), t));
//            log.info("loaded bookmarks.");
//        } else {
//            log.info("No bookmark file found.  Will create a new one on first update.");
//            _bookmarks = new java.util.TreeSet<>();
//            _tags = new java.util.TreeSet<>();
//        }
//    }
//    
//    public BookmarkCollection save() throws IOException {
//        // TODO: use atomic files
//        synchronized(_lock) {
//            log.debug("saving bookmarks...");
//            SavedCollection sc = new SavedCollection();
//            sc.bookmarks = _bookmarks;
//            sc.tags = _tags;
//            Json.toJson(sc, _file);
//            log.debug("saved.");
//        }
//        return this;
//    }
//    
//    private BookmarkCollection dump() throws IOException { // TODO: delete
//        SavedCollection sc = new SavedCollection();
//        sc.bookmarks = _bookmarks;
//        sc.tags = _tags;
//        System.out.println(Json.toJson(sc));
//        return this;
//    }
//    
//    public BookmarkCollection upsert(Tag t) throws IOException {
//        synchronized(_lock) {
//            _tags.remove(t);
//            _tags.add(t);
//            _tagsByNormalizedName.put(t.normalizedName(), t);
//            save();
//        }
//        return this;
//    }
//    
//    public Bookmark upsert(Bookmark b) throws IOException {
//        return upsert(b, null);
//    }
//
//    private Bookmark deleteWithoutSaving(String url) {
//        url = Strings.safeTrimToNull(url);
//        if (url == null) return null;
//        synchronized(_lock) {
//            Bookmark result = _bookmarksByUrl.remove(url);
//            if (result != null) _bookmarks.remove(result);
//            return result;
//        }
//    }
//    
//    private void ensureTagFor(String tagName) {
//        if (tagName == null) return;
//        tagName = Tag.normalizeName(tagName);
//        log.debug("ensuring tag name for {}", tagName);
//        synchronized(_lock) {
//            if (!_tagsByNormalizedName.containsKey(tagName)) {
//                log.debug("creating new tag.");
//                Tag tag = Tag.newBuilder().name(tagName).build();
//                _tags.add(tag);
//                _tagsByNormalizedName.put(tagName, tag);
//            }
//        }
//    }
//    
//    public Bookmark upsert(Bookmark b, String oldUrl) throws IOException {
//        synchronized(_lock) {
//            deleteWithoutSaving(oldUrl);
//            deleteWithoutSaving(b.url()); // always have to remove; treeset doesn't replace on add of equivalent
//
//            _bookmarks.remove(b); 
//            _bookmarks.add(b);
//            _bookmarksByUrl.put(b.url(), b);
//            b.tags().tagNames().stream().forEach(s -> ensureTagFor(s));
//            save();
//        }
//        return b;
//        
//    }
//    
//    public Bookmark delete(Bookmark b) throws IOException {
//        if (b == null) return null;
//        synchronized(_lock) {
//            Bookmark result = deleteWithoutSaving(b.url());
//            if (result != null) save(); // if null then nothing was removed so no need to save
//            return result;
//        }
//    }
//
//    public Bookmark deleteByUrl(String url) throws IOException {
//        synchronized(_lock) {
//            return delete(findByUrl(url));
//        }
//    }
//    
//    public Bookmark findByUrl(String url) {
//        if (url == null) return null;
//        synchronized(_lock) {
//            return _bookmarksByUrl.get(Strings.safeTrimToNull(url));
//        }
//    }
//
//    public Set<Tag> tags() {
//        return Collections.unmodifiableSet(_tags);
//    }      
//    
//    public Set<Bookmark> bookmarks() {
//        return Collections.unmodifiableSet(_bookmarks);
//    }
//  
//    public Set<Bookmark> bookmarks(String query) {
//        TagNameSet qtags = new TagNameSet(query);
//        synchronized(_lock) {
//            if (qtags.isEmpty()) return bookmarks();           
//            return _bookmarks.stream()
//                    .filter(b -> b.tags().containsAll(qtags))
//                    .collect(Collectors.toCollection(java.util.TreeSet::new));
//        }
//    }
//    
//    private class SavedCollection {
//        public TreeSet<Tag> tags;
//        public TreeSet<Bookmark> bookmarks;
//    }
//    
//    public static void main(String[] args) throws Exception {        
//        JsonConfig.init();
//
//        BookmarkCollection bc = new BookmarkCollection(Paths.get("/home/mlamb/bookmarks.json"));
////        bc.upsert(Tag.newBuilder().name("toread").build());
////        bc.upsert(Tag.newBuilder().name("programming").build());
////        bc.upsert(Bookmark.newBuilder().url("http://martylamb.com").notes("should be overwritten").title("Marty Lamb").build());
////        bc.upsert(Bookmark.newBuilder().url("http://martylamb.com").notes("my website").title("Marty Lamb").tags("personal me").build());
////        bc.upsert(Bookmark.newBuilder().url("http://martiansoftware.com").notes("my website").title("Marty Lamb").tags("work me").build());
////        bc.upsert(Bookmark.newBuilder().url("http://rajant.com").notes("Rajant Corp.").title("Marty Lamb").tags("work me").build());
////        bc.upsert(Bookmark.newBuilder().url("http://google.com").notes("a search engine").title("google").tags("search").build());
////        bc.save();
//        bc.dump();
//    }
}
