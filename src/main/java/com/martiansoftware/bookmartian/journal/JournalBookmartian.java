package com.martiansoftware.bookmartian.journal;

import com.martiansoftware.bookmartian.model.Bookmark;
import com.martiansoftware.bookmartian.model.Bookmartian;
import com.martiansoftware.bookmartian.model.Lurl;
import com.martiansoftware.bookmartian.model.Tag;
import com.martiansoftware.bookmartian.model.TagName;
import com.martiansoftware.boom.Json;
import com.martiansoftware.tinyjournal.TinyFileJournal;
import com.martiansoftware.tinyjournal.TinyJournal;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

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
    
    public JournalBookmartian(Path journalPath) throws IOException {
        _journalPath = journalPath;
        _journal = new TinyFileJournal(journalPath);
        _journal.stream(e -> e.printStackTrace())
            .map(journalEntry -> BMJournalEntry.from(journalEntry))
            .forEach(je -> apply(je));
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
            Optional<Bookmark> result = Optional.ofNullable(_bookmarks.remove(lurl));
            result.ifPresent(b -> writeAndApply(new BMJournalEntry().delete(lurl)));
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
            Bookmark b = bookmark.merge(get(bookmark.lurl()));
            if (replacing != null) {
                b = bookmark.merge(get(replacing));
                if (!bookmark.lurl().equals(replacing)) je.delete(replacing);
            }
            je.add(b);
            // TODO cleanup tags?
            writeAndApply(je);
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
    
    public class JournalException extends RuntimeException {
        public JournalException(String msg, Exception rootCause) {
            super(msg, rootCause);
        }
    }
}
