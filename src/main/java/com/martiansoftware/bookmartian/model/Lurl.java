package com.martiansoftware.bookmartian.model;

import com.martiansoftware.util.Strings;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A lenient url class; instances don't have to be valid urls, but the ones that
 * appear to be have normalized schemas and domain names for known schemas
 * 
 * @author mlamb
 */
public class Lurl implements Comparable<Lurl> {
   
    private static final Logger log = LoggerFactory.getLogger(Lurl.class);
    
    // used to see if a url has any scheme at all
    private static final Pattern SCHEME_FINDER = Pattern.compile("^(?<scheme>[a-zA-z0-9+-.]+):.*");
    
    // used to trim trailing slashes from paths with no query or fragment
    private static final Pattern PATH_TRIMMER = Pattern.compile("^(?<path>[^?#]*)/+$");
    
    private static final Pattern WEBLIKE_URL = Pattern.compile("^(?<scheme>[a-zA-z0-9+-.]+)"
                                                                + "://"
                                                                + "(?<credentials>"
                                                                    + "(?<username>[^:/]+)"
                                                                    + ":"
                                                                    + "(?<password>[^:/]+)"
                                                                + "@)?" // end of optional credentials
                                                                + "(?<host>[^:/\\s]+)"
                                                                + "(?<port>:[0-9]+)?" // optional port
                                                                + "(?<rest>/.*)?"); // optional remaining pathinfo, etc.
    
    // see isWeblike() for description
    private static final Set<String> WEBLIKE_SCHEMES = new java.util.HashSet<>(Arrays.asList("http", "https", "ftp", "ftps", "sftp", "ws", "wss"));
    
    // these schemas always get the WHOLE url lowercased
    private static final Set<String> ALWAYS_LOWERCASE_ENTIRE_URL = new java.util.HashSet<>(Arrays.asList("mailto"));
    
    
    private static final Pattern GENERIC_URL = Pattern.compile("^(?<scheme>[a-zA-z0-9+-.]+)"
                                                                 + "://"
                                                                 + "(?<rest>.*)?"); // literally anything else
    
    private static final String UNC_FROM_HOST_PATTERN = "(?<host>[^\\\\]+)(?<rest>\\\\.*[^\\\\])?\\\\*"; // pattern strips trailing backslashes
    
    private static final String LONG_UNC_PREFIX = "\\\\?\\UNC\\";
    private static final Pattern LONG_UNC = Pattern.compile("^" 
                                                                + Pattern.quote(LONG_UNC_PREFIX)
                                                                + UNC_FROM_HOST_PATTERN
                                                                + "$");
    
    private static final String GENERIC_UNC_PREFIX = "\\\\";
    private static final Pattern GENERIC_UNC = Pattern.compile("^" 
                                                                + Pattern.quote(GENERIC_UNC_PREFIX)
                                                                + UNC_FROM_HOST_PATTERN
                                                                + "$");
    
    private final String _lurl;
        
    static {
        System.out.println(LONG_UNC.pattern());
    }
    
    private Lurl(String url) {
        _lurl = normalize(url);
    }
    
    public static Lurl of(String url) {
        return new Lurl(url);
    }
    
    // used to determine if the url scheme is "weblike", meaning we can use the
    // weblikeUrl pattern to parse it and can safely lowercase the host.  Some
    // schemes may not even include a host so we can't just pick out what looks
    // like a host and lowercase it since that might break the url.
    private boolean isWeblike(String scheme) {
        return WEBLIKE_SCHEMES.contains(scheme.toLowerCase());
    }
    
    private void appendIfNotEmpty(StringBuilder sb, String s) {
        if (!Strings.isEmpty(s)) sb.append(s);
    }
    
    private String maybeLowercaseWholeUrl(StringBuilder sb, String scheme) {
        return ALWAYS_LOWERCASE_ENTIRE_URL.contains(scheme.toLowerCase())
                ? sb.toString().toLowerCase()
                : sb.toString();
    }
    
    private String scrubRest(String rest) {
        if (rest == null) return null;
        // if the URL has no fragment or query, trim all trailing slashes
        Matcher m = PATH_TRIMMER.matcher(rest);
        if (m.matches()) {
            return m.group("path");
        } else {
            return rest;
        }
    }

    private String buildUnc(String prefix, Matcher m) {
        StringBuilder result = new StringBuilder(prefix);
        result.append(m.group("host").toLowerCase());
        appendIfNotEmpty(result, m.group("rest"));
        return result.toString();
    }
    
    private String normalizeUnc(String uncPath) {
        Matcher m = LONG_UNC.matcher(uncPath);
        if (m.matches()) return buildUnc(LONG_UNC_PREFIX, m);
        m = GENERIC_UNC.matcher(uncPath);
        if (m.matches()) return buildUnc(GENERIC_UNC_PREFIX, m);
        log.warn("weird UNC path \"{}\", leaving as-is.", uncPath);
        return uncPath;
    }
    
    private boolean isUnc(String s) {
        return s.startsWith(GENERIC_UNC_PREFIX);
    }
    
    private String normalize(String url) {
        // TODO: windows path check
        String result = url.trim();
        if (result.length() == 0) throw new IllegalArgumentException("url may not be empty");

        if (isUnc(result)) return normalizeUnc(result);
        
        // no scheme at all?  assume it's http and try again
        Matcher m = SCHEME_FINDER.matcher(result);
        if (!m.matches()) return normalize("http://" + url);
        
        m = WEBLIKE_URL.matcher(result);        
        if (m.matches() && isWeblike(m.group("scheme"))) {
            StringBuilder s = new StringBuilder();
            s.append(m.group("scheme").toLowerCase());
            s.append("://");
            appendIfNotEmpty(s, m.group("credentials"));
            s.append(m.group("host").toLowerCase());            
            appendIfNotEmpty(s, m.group("port"));
            appendIfNotEmpty(s, scrubRest(m.group("rest")));
            result = maybeLowercaseWholeUrl(s, m.group("scheme"));
        } else {
            m = GENERIC_URL.matcher(result);
            if (m.matches()) {
                StringBuilder s = new StringBuilder();
                s.append(m.group("scheme").toLowerCase());
                s.append("://");
                appendIfNotEmpty(s, scrubRest(m.group("rest")));
                result = maybeLowercaseWholeUrl(s, m.group("scheme"));
            } else {
                log.warn("weird URL \"{}\", leaving as-is.", result);
            }
        }
        return result;
    }
    
    @Override
    public String toString() {
        return _lurl;
    }

    @Override
    public int compareTo(Lurl other) {
        if (other == null) return 1;
        return _lurl.compareTo(other._lurl);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this._lurl);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Lurl other = (Lurl) obj;
        if (!Objects.equals(this._lurl, other._lurl)) {
            return false;
        }
        return true;
    }
    
    // gson helper
    public static class GsonAdapter extends JsonConfig.StringAdapter<Lurl> {
        @Override protected String toString(Lurl lurl ){ return lurl.toString(); }
        @Override protected Lurl fromString(String s) { return Lurl.of(s); }
        @Override public Stream<Class> classes() { return Stream.of(Lurl.class); }
    }
 
    
    public static void main(String[] args) throws Exception {
        Lurl l = Lurl.of("\\\\?\\UNC\\Thing\\with\\Path");
        System.out.println(l);
    }
}
