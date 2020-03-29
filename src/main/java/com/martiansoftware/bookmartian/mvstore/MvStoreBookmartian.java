package com.martiansoftware.bookmartian.mvstore;

import com.martiansoftware.bookmartian.model.Bookmark;
import com.martiansoftware.bookmartian.model.Bookmartian;
import com.martiansoftware.bookmartian.model.Lurl;
import com.martiansoftware.bookmartian.model.Tag;
import com.martiansoftware.bookmartian.model.TagName;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mlamb
 */
public class MvStoreBookmartian implements Bookmartian {

    private static final Logger log = LoggerFactory.getLogger(MvStoreBookmartian.class);
    private static final String _mapPrefix = ""; // TEMPORARY UNTIL WE HAVE USERS
    
    private final Path _dataDir;
    private transient final Object _lock = new Object();
    private final MVStore _store;
    
    private final MVMap<String, Bookmark.Builder> _bookmarkBuilders;
    private final MVMap<String, Tag.Builder> _tagBuilders;
    
    public MvStoreBookmartian(Path dataDir) throws IOException {
        log.info("using data directory: {}", dataDir);
        _dataDir = dataDir;
        if (!Files.exists(_dataDir)) {
            log.info("creating directory: {}", _dataDir);
            Files.createDirectories(_dataDir);
        }
        if (!Files.isDirectory(_dataDir)) {
            log.error("{} is not a directory!", _dataDir);
            throw new IOException(_dataDir + " is not a directory!");
        }
        String dataname = dataDir.resolve("bookmartian-data").toAbsolutePath().toString();
        log.info("using data file: {}", dataname);
        _store = MVStore.open(dataname);
        _bookmarkBuilders = _store.openMap(_mapPrefix + ":bookmarks");
        _tagBuilders = _store.openMap(_mapPrefix + ":tags");
    }
    
    @Override
    public Collection<Bookmark> bookmarks() {
        return _bookmarkBuilders.entrySet().stream()
                .map(e -> e.getValue().build())
                .collect(Collectors.toCollection(() -> new java.util.ArrayList<>(_bookmarkBuilders.size())));
    }

    private Optional<Bookmark> obm(Bookmark.Builder b) {
        return b == null ? Optional.empty() : Optional.of(b.build());
    }
    
    @Override
    public Optional<Bookmark> get(Lurl lurl) {
        return obm(_bookmarkBuilders.get(lurl.toString()));
    }

    @Override
    public Optional<Bookmark> remove(Lurl lurl) {
        return obm(_bookmarkBuilders.remove(lurl.toString()));
    }

    
    @Override
    public Optional<Bookmark> visit(Lurl lurl) {
        Optional<Bookmark> toUpdate = get(lurl);
        if (toUpdate.isPresent()) {
            Bookmark old = toUpdate.get();
            Bookmark b = old.toBuilder()
                            .visitCount(old.visitCount().map(v -> v + 1).orElse(1l))
                            .lastVisited(new java.util.Date())
                            .build();
            update(lurl, b);
            return Optional.ofNullable(b);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Bookmark update(Lurl replacing, Bookmark bookmark) {
        _bookmarkBuilders.put(bookmark.lurl().toString(), bookmark.toBuilder());
        if (replacing != null && !replacing.equals(bookmark.lurl())) remove(replacing);
        
        // always make sure any newly-added tags are stored
        for (TagName tn : bookmark.tagNames().asSet()) {
            if (!_tagBuilders.containsKey(tn.toString())) {
                _tagBuilders.put(tn.toString(), Tag.of(tn).toBuilder());
            }
        }
        return bookmark;
    }

    @Override
    public Optional<String> config() throws IOException {
        Path configFile = _dataDir.resolve("config.json");
        if (Files.exists(configFile)) {
            return Optional.of(Files.readAllLines(configFile).stream().collect(Collectors.joining("\n")));
        }
        return Optional.empty();
    }

    @Override
    public Collection<Tag> tags() {       
        return _tagBuilders.entrySet().stream()
                .map(e -> e.getValue().build())
                .collect(Collectors.toCollection(() -> new java.util.ArrayList<>(_tagBuilders.size())));
    }

    private Optional<Tag> otag(Tag.Builder t) {
        return t == null ? Optional.empty() : Optional.of(t.build());
    }

    @Override
    public Optional<Tag> get(TagName tn) {
        return otag(_tagBuilders.get(tn.toString()));
    }

    @Override
    public Tag update(Tag tag) {
        _tagBuilders.put(tag.tagName().toString(), tag.toBuilder());
        return tag;
    }

    @Override
    public void shutdown() {
        _store.close();
    }
    
}
