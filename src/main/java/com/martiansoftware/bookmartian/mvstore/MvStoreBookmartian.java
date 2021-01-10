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

    private static final Logger LOG = LoggerFactory.getLogger(MvStoreBookmartian.class);
    private final String _mapPrefix = ""; // FIXME: TEMPORARY UNTIL WE HAVE USERS
    
    private final Path _dataDir;
    private transient final Object _lock = new Object();
    private final MVStore _store;
    
    private final MVMap<String, Bookmark.Builder> _bookmarkBuilders; // keys are Lurls
    private final MVMap<String, Tag.Builder> _tagBuilders;
    private boolean _isClosed = false;
    
    public MvStoreBookmartian(Path dataDir) throws IOException {
        LOG.info("using data directory: {}", dataDir);
        _dataDir = dataDir;
        if (!Files.exists(_dataDir)) {
            LOG.info("creating directory: {}", _dataDir);
            Files.createDirectories(_dataDir);
        }
        if (!Files.isDirectory(_dataDir)) {
            LOG.error("{} is not a directory!", _dataDir);
            throw new IOException(_dataDir + " is not a directory!");
        }
        String dataname = dataDir.resolve("bookmartian-data").toAbsolutePath().toString();
        LOG.info("using data file: {}", dataname);
        _store = MVStore.open(dataname);
        
        _bookmarkBuilders = _store.openMap(_mapPrefix + ":bookmarks"); // need to re-think map prefix.  maybe use a different mvstore for each user for better portability?
        _tagBuilders = _store.openMap(_mapPrefix + ":tags");
        
        shutdownOnVmExit();
    }
    
    @Override
    public Collection<Bookmark> bookmarks() {
        synchronized(_lock) {
            return _bookmarkBuilders.entrySet().stream()
                    .map(e -> e.getValue().build())
                    .collect(Collectors.toCollection(() -> new java.util.ArrayList<>(_bookmarkBuilders.size())));
        }
    }

    private Optional<Bookmark> obm(Bookmark.Builder b) {
        return b == null ? Optional.empty() : Optional.of(b.build());
    }
    
    @Override
    public Optional<Bookmark> get(Lurl lurl) {
        synchronized(_lock) {
            return obm(_bookmarkBuilders.get(lurl.toString()));
        }
    }

    @Override
    public Optional<Bookmark> remove(Lurl lurl) {
        synchronized(_lock) {
            return obm(_bookmarkBuilders.remove(lurl.toString()));
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
                update(lurl, b);
                return Optional.ofNullable(b);
            } else {
                return Optional.empty();
            }
        }
    }

    @Override
    public Bookmark update(Lurl replacing, Bookmark bookmark) {
        synchronized(_lock) {
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
    }

    @Override
    public Optional<String> config() throws IOException {
        synchronized(_lock) {
            Path configFile = _dataDir.resolve("config.json");
            if (Files.exists(configFile)) {
                LOG.info("Loading config from {}", configFile.toAbsolutePath());
                return Optional.of(Files.readAllLines(configFile).stream().collect(Collectors.joining("\n")));
            }
            LOG.info("No configuration file at {}", configFile.toAbsolutePath());
            return Optional.empty();
        }
    }

    @Override
    public Collection<Tag> tags() {       
        synchronized(_lock) {
            return _tagBuilders.entrySet().stream()
                    .map(e -> e.getValue().build())
                    .collect(Collectors.toCollection(() -> new java.util.ArrayList<>(_tagBuilders.size())));
        }
    }

    private Optional<Tag> otag(Tag.Builder t) {
        return t == null ? Optional.empty() : Optional.of(t.build());
    }

    @Override
    public Optional<Tag> get(TagName tn) {
        synchronized(_lock) {
            return otag(_tagBuilders.get(tn.toString()));
        }
    }

    @Override
    public Tag update(Tag tag) {
        synchronized(_lock) {
            _tagBuilders.put(tag.tagName().toString(), tag.toBuilder());
            return tag;
        }
    }

    @Override
    public void shutdown() {
        synchronized(_lock) {
            if (!_isClosed) {
                LOG.info("shutting down...");
                _store.close();
                LOG.info("shut down.");
                _isClosed = true;
            }
        }
    }
    
    private void shutdownOnVmExit() {        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override public void run() {
                shutdown();
            }
        });
        LOG.info("added shutdown hook");
    }
}
