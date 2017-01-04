package com.martiansoftware.bookmartian.jsondir;

import com.martiansoftware.boom.Json;
import com.martiansoftware.util.Strings;
import com.martiansoftware.validation.Hope;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Map-like class that persists data to json files in a dedicated directory.
 * This class is NOT thread-safe.
 * The backing map is a treemap, so keys are ordered.
 * 
 * @author mlamb
 */
class JsonDirMap<K, V> {

    private static final Logger log = LoggerFactory.getLogger(JsonDirMap.class);
    
    private final Path _dir; // dir housing the json files
    private final Class<V> _vClass; // value class (the type serialized to json)
    private final String _kDesc; // a description of the key type (used for logging only)
    private final Function<V, K> _keyGetter; // transforms values to keys
//    private final Map<K, String> _keysToFilenames = new java.util.HashMap<>(); // maps each key to its associated filename within _dir
    private final Map<K, ValueAndFilename<V>> _map = new java.util.TreeMap<>();
    
    private JsonDirMap(Path dir, Class<V> vClass, Function<V, K> keyGetter, String kDesc) throws IOException {
        _dir = Hope.that(dir).named("path").isNotNull().value();
        _vClass = Hope.that(vClass).named("value class").isNotNull().value();
        _keyGetter = Hope.that(keyGetter).named("keyGetter").isNotNull().value();
        _kDesc = kDesc == null ? "key" : kDesc;
        load();
    }
    
    private void load() throws IOException {
        Files.list(_dir)
            .filter(p -> isJsonFile(p))
            .sorted((p1, p2) -> p1.getFileName().compareTo(p2.getFileName()))
            .forEach(p -> maybeLoad(p));
    }
    
    private void maybeLoad(Path p) {
        try {
            log.debug("loading {}", p);
            V v = Json.fromJson(p, _vClass);
            K k = _keyGetter.apply(v);
            if (_map.containsKey(k)) {
                log.warn("ignoring {}: duplicate {} already loaded from {}", p, _kDesc, _map.get(k).value);
            } else {
                _map.put(k, new ValueAndFilename(v, p.getFileName().toString()));
            }
        } catch (IOException e) {
            log.error("unable to load {}: {}", p, e.getMessage());
        }
    }
    
    private Path newFilePath() {
        Path result;
        while (true) {
            String filename = String.format("%s.json", IdGenerator.generate());
            if (filename.startsWith("-")) continue; // filenames starting with - are a PITA to work with on command line
            
            result = _dir.resolve(filename);
            if (Files.exists(result)) continue; // don't allow dupes in filesystem

            return result;
        }
    }
    
    private Path pathFor(V value) {
        ValueAndFilename vaf = _map.get(_keyGetter.apply(value));
        return (vaf == null) ? null : _dir.resolve(vaf.filename);
    }
    
    public JsonDirMap<K, V> add(V value) throws IOException {
        if (value != null) {
            K key = _keyGetter.apply(value);
            Path origPath = pathFor(value);
            Path dest = newFilePath();
            log.debug("saving object with {} [{}] to {}", _kDesc, key, dest);
            Json.toJson(value, dest);
            _map.put(key, new ValueAndFilename(value, nameOf(dest)));
            if (origPath != null) Files.delete(origPath);
        }
        return this;
    }
    
    private Path pathOf(String filename) {
        return _dir.resolve(filename);
    }
    
    private String nameOf(Path p) {
        return p.getFileName().toString();
    }
    
    public V remove(K key) throws IOException {
        V result = null;
        if (key != null) {
            ValueAndFilename<V> vaf = _map.remove(key);
            if (vaf != null) result = vaf.value;
            try (Deleter d = new Deleter(vaf)) {
                if (d.hasFile()) {
                    log.debug("deleting object with {} {} from {}", _kDesc, key, d.filePath());
                    d.commit();
                } else {
                    log.warn("no files associated with key [{}]; nothing to delete", key);
                }
            }
        }
        return result;
    }
    
    public int size() {
        return _map.size();
    }
    
    public V get(K key) {
        ValueAndFilename<V> vaf = _map.get(key);
        return vaf == null ? null : vaf.value;
    }
    
    public V removeByValue(V t) throws IOException {
        return remove(_keyGetter.apply(t));
    }
    
    public Stream<V> values() {
        return _map.values().stream().map(vaf -> vaf.value);
    }
    
    private boolean isJsonFile(Path p) {
        return Files.isRegularFile(p) && Strings.lower(p.getFileName().toString()).endsWith(".json");        
    }
    
    public static <K, T> JsonDirBuilder<K, T> newBuilder() {
        return new JsonDirBuilder<>();
    }
    
    public static class JsonDirBuilder<K, V> {
        private Path _dir;
        private Class<V> _vClass;
        private Function<V, K> _keyGetter;
        private String _keyDesc;
        
        public JsonDirBuilder path(Path dir) {
            _dir = dir;
            return this;
        }
        
        public JsonDirBuilder valueClass(Class<V> vClass) {
            _vClass = vClass;
            return this;
        }
        
        public JsonDirBuilder keyGetter(Function<V, K> keyGetter) {
            _keyGetter = keyGetter;
            return this;
        }
        
        public JsonDirBuilder keyDesc(String keyDesc) {
            _keyDesc = keyDesc;
            return this;
        }
                
        public JsonDirMap build() throws IOException {
            return new JsonDirMap(_dir, _vClass, _keyGetter, _keyDesc);
        }
    }
    
    private static class IdGenerator {
        private static final Random R = new Random();
        private static final char[] IDCHARS = ("abcdefghijkmnopqrstuvwxyz" 
                                                + "ABCDEFGHJKLMNPQRSTUVWXYZ"
                                                + "23456789-_").toCharArray();
        
        // 59 chars in IDCHARS, so 714,924,299 permutations of 5-char IDs
        public static String generate() {            
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < 5; ++i) {
                s.append(IDCHARS[R.nextInt(IDCHARS.length)]);
            }
            return s.toString();
        }
    }
    
    private class ValueAndFilename<V> {
        final V value;
        final String filename;
        ValueAndFilename(V newValue, String newFilename) {
            value = newValue;
            filename = newFilename;
        }
    }

    // helper class to delete a specified path when closed
    private class Deleter implements AutoCloseable {
        private final Path _p;
        private boolean _committed = false;
        
        public Deleter(ValueAndFilename vaf) {
            _p = vaf == null ? null : pathOf(vaf.filename);
        }

        private boolean hasFile() {
            return _p != null;
        }
        
        private Path filePath() {
            return _p;
        }
        
        public void commit() {
            _committed = true;            
        }
        
        @Override
        public void close() throws IOException {
            if (_p != null && _committed) Files.deleteIfExists(_p);
        }
    }

}
