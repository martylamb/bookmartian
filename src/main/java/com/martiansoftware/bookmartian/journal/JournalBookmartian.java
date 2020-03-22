package com.martiansoftware.bookmartian.journal;

import com.martiansoftware.bookmartian.model.Bookmark;
import com.martiansoftware.bookmartian.model.Bookmartian;
import com.martiansoftware.bookmartian.model.Lurl;
import com.martiansoftware.bookmartian.model.Tag;
import com.martiansoftware.bookmartian.model.TagName;
import com.martiansoftware.bookmartian.model.TagNameSet;
import com.martiansoftware.boom.Json;
import com.martiansoftware.tinyjournal.TinyFileJournal;
import com.martiansoftware.tinyjournal.TinyJournal;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mlamb
 */
public class JournalBookmartian implements Bookmartian {
    
    private final Path _journalPath;
    private final TinyJournal _journal;
    private final Map<Lurl, Bookmark> _bookmarks = new java.util.TreeMap<>();
    private final Map<TagName, Tag> _tags = new java.util.TreeMap<>();
    private final Object _lock = new Object();
    private static final Logger log = LoggerFactory.getLogger(JournalBookmartian.class);
    
    
    public JournalBookmartian(Path journalPath) throws IOException {
        _journalPath = journalPath;
        
        log.info("using bookmark journal: {}", journalPath);
        Path parent = journalPath.getParent();
        if (!Files.exists(parent)) {
            log.info("creating directory: {}", parent);
            Files.createDirectories(parent);
        }
        if (!Files.isDirectory(parent)) {
            log.error("{} is not a directory!", parent);
            throw new IOException(parent + " is not a directory!");
        }
        _journal = new TinyFileJournal(journalPath);
        _journal.stream(e -> e.printStackTrace())
            .map(journalEntry -> BMJournalEntry.from(journalEntry))
            .forEach(je -> apply(je));
        ensureTagsForAllBookmarks();
        autoDeleteUnusedTags(TagNameSet.of(_tags.keySet()));
        log.info("finished loading {}", journalPath);
        log.info("loaded {} bookmarks", _bookmarks.size());
    }

    private void apply(BMJournalEntry je) {
        if (je.isCheckpoint()) {
            _bookmarks.clear();
            _tags.clear();
        }
        je.bookmarksToDelete().forEach(lurl -> _bookmarks.remove(lurl));
        je.tagsToDelete().forEach(tagName -> _tags.remove(tagName));
        je.bookmarks().forEach(b -> _bookmarks.put(b.lurl(), b));
        je.tags().forEach(t -> _tags.put(t.tagName(), t));
    }

    private void writeAndApply(BMJournalEntry je) throws JournalException {
        try {
            _journal.writeString(Json.toJson(je));
            apply(je);
        } catch (IOException e) {
            throw new JournalException("error writing to journal " + _journalPath, e);
        }
    }
    
    @Override
    public Collection<Bookmark> bookmarks() {
        synchronized(_lock) {
            return Collections.unmodifiableCollection(new java.util.ArrayList<>(_bookmarks.values()));
        }
    }

    @Override
    public Optional<Bookmark> get(Lurl lurl) {
        synchronized(_lock) {
            return Optional.ofNullable(_bookmarks.get(lurl));
        }
    }

    @Override
    public Optional<Bookmark> remove(Lurl lurl) {
        synchronized(_lock) {
            Optional<Bookmark> result = Optional.ofNullable(_bookmarks.get(lurl));            
            if (result.isPresent()) {
                writeAndApply(new BMJournalEntry().delete(lurl));
                autoDeleteUnusedTags(result.get().tagNames());
            }
            return result;
        }
    }

    @Override
    public Optional<Bookmark> visit(Lurl lurl) {
        synchronized(_lock) {
            Optional<Bookmark> toUpdate = get(lurl);
            if (toUpdate.isPresent()) {
                Bookmark old = toUpdate.get();
                Bookmark b = old.toBuilder()
                                .visitCount(old.visitCount().map(v -> v + 1).orElse(1l))
                                .lastVisited(new java.util.Date())
                                .build();
                writeAndApply(new BMJournalEntry().add(b));
                return Optional.ofNullable(b);
            } else {
                return Optional.empty();
            }
        }
    }

    @Override
    public Tag update(Tag tag) {
        synchronized(_lock) {
            BMJournalEntry je = new BMJournalEntry();
            je.add(tag);
            writeAndApply(je);
            return tag;
        }
    }
    
    @Override
    public Bookmark update(Lurl replacing, Bookmark bookmark) {
        synchronized(_lock) {
            BMJournalEntry je = new BMJournalEntry();
            Bookmark orig = _bookmarks.get(bookmark.lurl());
            Bookmark b = bookmark.merge(get(bookmark.lurl()));
            if (replacing != null) {
                b = bookmark.merge(get(replacing));
                orig = _bookmarks.get(replacing);
                if (!bookmark.lurl().equals(replacing)) je.delete(replacing);
            }
            ensureTags(b.tagNames(), je);
            je.add(b);
            writeAndApply(je);
            
            // skip tag cleanup if there's no way any tags have been removed
            // (i.e., it's a new bookmark or an edited one with the same tag set)
            if (orig != null && !orig.tagNames().equals(b.tagNames())) {
                TagNameSet maybeUnusedTags = TagNameSet.of(
                    Stream.concat(orig.tagNames().asSet().stream(), b.tagNames().asSet().stream())
                            .collect(Collectors.toSet())
                );                        
                autoDeleteUnusedTags(maybeUnusedTags);
            }
            
            return b;
        }
    }

    @Override
    public Collection<Tag> tags() {
        synchronized(_lock) {
            return Collections.unmodifiableCollection(new java.util.ArrayList<>(_tags.values()));
        }
    }

    @Override
    public Optional<Tag> get(TagName tn) {
        synchronized(_lock) {
            return Optional.ofNullable(_tags.get(tn));
        }
    }

    @Override
    public void shutdown() {
        synchronized(_lock) {
            _journal.close();
        }
    }
    
    private void ensureTags(TagNameSet tagNames, BMJournalEntry je) {
        synchronized(_lock) {
            tagNames.asSet().stream()
                .filter(tn -> !_tags.containsKey(tn))
                .map(tn -> Tag.of(tn))
                .peek(t -> log.info("auto-creating tag {}", t.tagName()))
                .forEach(t -> je.add(t));
        }                    
    }
    
    private void ensureTagsForAllBookmarks() {
        synchronized(_lock) {
            BMJournalEntry je = new BMJournalEntry();
            _bookmarks.values().stream()
                .flatMap(bm -> bm.tagNames().asSet().stream())
                .filter(tn -> !_tags.containsKey(tn))
                .distinct()                    
                .map(tn -> Tag.of(tn))
                .peek(t -> log.info("auto-creating tag {}", t.tagName()))
                .forEach(tag -> je.add(tag));
            
            if (je.tags().count() > 0) writeAndApply(je);
        }
    }
    
    // of the specified tagnames, auto-delete the ones that no bookmark uses
    private void autoDeleteUnusedTags(TagNameSet tagsToMaybeDelete) {
        if (tagsToMaybeDelete.isEmpty()) return;
        synchronized(_lock) {
            Set<TagName> tagsToDelete = new java.util.HashSet<>(tagsToMaybeDelete.asSet());
            tagsToDelete.removeAll(_bookmarks.values().stream()
                                    .flatMap(b -> b.tagNames().asSet().stream())
                                    .collect(Collectors.toCollection(java.util.HashSet::new)));
            
            BMJournalEntry je = new BMJournalEntry();
            for (TagName tn : tagsToDelete) {
                log.info("auto-deleting tag [{}]", tn);
                je.delete(tn);
            }
            if (je.tagsToDelete().count() > 0) writeAndApply(je);            
        }
    }
    
    public class JournalException extends RuntimeException {
        public JournalException(String msg, Exception rootCause) {
            super(msg, rootCause);
        }
    }
}
