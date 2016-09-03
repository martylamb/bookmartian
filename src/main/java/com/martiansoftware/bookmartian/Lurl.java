package com.martiansoftware.bookmartian;

import com.martiansoftware.util.Strings;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A lenient url class; instances don't have to be valid urls, but the ones that
 * appear to be have normalized schemas and domain names for known schemas
 * 
 * @author mlamb
 */
public class Lurl {
   
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
    
    public Lurl(String url) {
        _lurl = normalize(url);
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
            }
        }
        return result;
    }
    
    @Override
    public String toString() {
        return _lurl;
    }
    
}
