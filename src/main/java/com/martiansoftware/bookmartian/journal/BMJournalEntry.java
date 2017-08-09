package com.martiansoftware.bookmartian.journal;

import com.martiansoftware.bookmartian.model.Bookmark;
import com.martiansoftware.bookmartian.model.Bookmartian;
import com.martiansoftware.bookmartian.model.Color;
import com.martiansoftware.bookmartian.model.Lurl;
import com.martiansoftware.bookmartian.model.Tag;
import com.martiansoftware.bookmartian.model.TagName;
import com.martiansoftware.boom.Json;
import com.martiansoftware.tinyjournal.JournalEntry;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class BMJournalEntry {

    private Boolean _cp = null;
    private Set<Lurl> _bd = null;
    private Set<TagName> _td = null;
    private List<Bookmark> _b = null;
    private List<Tag> _t = null;
    
    public static BMJournalEntry from(JournalEntry s) {
        System.out.println(s.readString());
        return Json.fromJson(s.readString(), BMJournalEntry.class);
    }
        
    public BMJournalEntry isCheckpoint(boolean isCheckpoint) {
        _cp = isCheckpoint ? Boolean.TRUE : null;
        return this;
    }
    
    public BMJournalEntry delete(Lurl lurl) {
        if (_bd == null) _bd = new java.util.HashSet<>();
        _bd.add(lurl);
        return this;
    }
    
    public BMJournalEntry delete(TagName tagName) {
        if (_td == null) _td = new java.util.HashSet<>();
        _td.add(tagName);
        return this;
    }
    
    public BMJournalEntry add(Bookmark bm) {
        if (_b == null) _b = new java.util.ArrayList<>();
        _b.add(bm);
        return this;
    }
    
    public BMJournalEntry add(Tag tag) {
        if (_t == null) _t = new java.util.ArrayList<>();
        _t.add(tag);
        return this;
    }
    
    public boolean isCheckpoint() {
        return _cp == null ? false : _cp;
    }
    
    public Stream<Lurl> bookmarksToDelete() {
        return _bd == null ? Stream.empty() : _bd.stream();
    }
    
    public Stream<TagName> tagsToDelete() {
        return _td == null ? Stream.empty() : _td.stream();
    }
    
    public Stream<Bookmark> bookmarks() {
        return _b == null ? Stream.empty() : _b.stream();
    }
    
    public Stream<Tag> tags() {
        return _t == null ? Stream.empty() : _t.stream();
    }
    
    public static void test(Bookmartian bm) {
        BMJournalEntry e = new BMJournalEntry();
        for (Bookmark b: bm.bookmarks()) {
            e.add(b);
        }
        e.add(Tag.newBuilder().color(Color.BLACK).name("deleteme").build());
        e.delete(TagName.of("deleteme"));
        e.delete(Lurl.of("http://martylamb.com"));
        System.out.println(Json.toJson(e));
    }
}
