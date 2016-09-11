package com.martiansoftware.bookmartian.model;

import com.martiansoftware.util.Strings;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * A lenient url class; instances don't have to be valid urls, but the ones that
 * appear to be have normalized schemas and domain names for known schemas
 * 
 * @author mlamb
 */
public class Lurl implements Comparable<Lurl> {
   
    private final String _lurl;
    
    // TODO: add pattern for mailto: urls?
    private final Pattern weblikeUrl = Pattern.compile("^(?<scheme>[a-zA-z0-9+-.]+)"
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
    private static final Set<String> weblikeSchemes = new java.util.HashSet<>(Arrays.asList("http", "https", "ftp", "ftps", "sftp", "ws", "wss"));
    
    // these schemas always get the WHOLE url lowercased
    private static final Set<String> alwaysLowercaseAll = new java.util.HashSet<>(Arrays.asList("mailto"));
    
    private final Pattern genericUrl = Pattern.compile("^(?<scheme>[a-zA-z0-9+-.]+)"
                                                        + "://"
                                                        + "(?<rest>.*)?"); // literally anything else
    
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
        return weblikeSchemes.contains(scheme.toLowerCase());
    }
    
    private void appendIfNotEmpty(StringBuilder sb, String s) {
        if (!Strings.isEmpty(s)) sb.append(s);
    }
    
    private String maybeLowercase(StringBuilder sb, String scheme) {
        return alwaysLowercaseAll.contains(scheme.toLowerCase())
                ? sb.toString().toLowerCase()
                : sb.toString();
    }
    
    private String normalize(String url) {
        String result = url.trim();
        Matcher m = weblikeUrl.matcher(result);        
        if (m.matches() && isWeblike(m.group("scheme"))) {
            StringBuilder s = new StringBuilder();
            s.append(m.group("scheme").toLowerCase());
            s.append("://");
            appendIfNotEmpty(s, m.group("credentials"));
            s.append(m.group("host").toLowerCase());            
            appendIfNotEmpty(s, m.group("port"));
            appendIfNotEmpty(s, m.group("rest"));            
            result = maybeLowercase(s, m.group("scheme"));
        } else {
            m = genericUrl.matcher(result);
            if (m.matches()) {
                StringBuilder s = new StringBuilder();
                s.append(m.group("scheme").toLowerCase());
                s.append("://");
                appendIfNotEmpty(s, m.group("rest"));
                result = maybeLowercase(s, m.group("scheme"));
            } else {
                result = normalize("http://" + result);
            }
        }
        if (result.length() == 0) throw new IllegalArgumentException("url may not be empty");
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
}
