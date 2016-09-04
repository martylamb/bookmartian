package com.martiansoftware.bookmartian;

import java.nio.file.Paths;
import static com.martiansoftware.boom.Boom.*;
import com.martiansoftware.boom.BoomResponse;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;
import com.martiansoftware.jsap.stringparsers.FileStringParser;
import com.martiansoftware.util.Strings;
import java.io.IOException;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Spark;

/**
 *
 * @author mlamb
 */
public class App {
    
    private static final String ARG_WEB_ROOT = "webroot";
    private static final String ARG_BOOKMARK_JSON_FILE = "BOOKMARK_FILE";
    
    private static Logger log = LoggerFactory.getLogger(App.class);
    
    private static JSAP jsap() throws JSAPException {
        JSAP jsap = new JSAP();

        jsap.registerParameter(new UnflaggedOption(ARG_BOOKMARK_JSON_FILE)
                                .setRequired(true));
        return jsap;
    }
    
    public static void main(String[] args) throws Exception {

        JSAP jsap = jsap();
        JSAPResult cmd = jsap().parse(args);
        if (!cmd.success()) {
            System.err.format("Usage: bookmartian %s%d", jsap.getUsage());
            System.exit(1);
        }
        
        JsonConfig.init();
        BookmarkCollection bc = new BookmarkCollection(Paths.get(cmd.getString(ARG_BOOKMARK_JSON_FILE)));
        
        before("/*", (req, rsp) -> log(req, rsp));
        before("/*", (req, rsp) -> disableCaching(req, rsp));
        
        get("/api/tags", () -> json(bc.tags()));        
        get("/api/bookmark", () -> getBookmark(bc));
        get("/api/bookmarks", () -> json(bc.bookmarks(request().queryParams("tags"))));
        
        options("/api/bookmark/update", () -> corsOptions());
        post("/api/bookmark/update", () -> updateBookmark(bc));
        
        post("/api/bookmark/delete", () -> deleteBookmark(bc));
    }
    
    private static BoomResponse corsOptions() {
        corsHeaders();
        return JSend.success();
    }
    
    private static void disableCaching(Request req, Response rsp) {
        rsp.header("Cache-Control", "no-cache, no-store, must-revalidate");
        rsp.header("Pragma", "no-cache");
        rsp.header("Expires", "0");
    }
    
    private static void corsHeaders() {
        String acrh = request().headers("Access-Control-Request-Headers");
        if (acrh != null) response().header("Access-Control-Allow-Headers", acrh);
        String acrm = request().headers("Access-Control-Request-Method");
        if (acrm != null) response().header("Access-Control-Allow-Methods", "POST");
        String origin = request().headers("Origin");
        response().header("Access-Control-Allow-Origin", origin == null ? "*" : origin);
    }

    private static String q(String key) {
        return Strings.safeTrimToNull(request().queryParams(key));
    }
    
    private static BoomResponse updateBookmark(BookmarkCollection bc) {
        String url = q("url"); // TODO: URL normalization (lowercase schema and domain name?)
        if (url == null) return JSend.fail("a URL is required");

        log.debug("updating bookmark: {}", url);
        String oldUrl = q("oldUrl");
        if (oldUrl != null) log.debug("  => replacing {}", oldUrl);
        
        Bookmark b = Bookmark.newBuilder()
                        .url(url)
                        .title(q("title"))
                        .imageUrl(q("imageUrl"))
                        .notes(q("notes"))
                        .tags(q("tags"))
                        .build();

        corsHeaders();
        try {
            return JSend.success(bc.upsert(b, oldUrl));
        } catch (IOException e) {
            return JSend.error(e);
        }
    }
    
    private static BoomResponse getBookmark(BookmarkCollection bc) {
        String url = q("url");
        if (url == null) return JSend.fail("a URL is required");
        log.debug("searching for bookmark: {}", url);
        Bookmark b = bc.findByUrl(url);
        return (b == null) ? JSend.fail("no such bookmark: " + url) : JSend.success(b);
    }
    
    private static BoomResponse deleteBookmark(BookmarkCollection bc) {
        String url = q("url"); // TODO: URL normalization (lowercase schema and domain name?)
        if (url == null) return JSend.fail("a URL is required");
        log.warn("deleting bookmark: {}", url);
        try {
            return JSend.success(bc.deleteByUrl(url));
        } catch (IOException e) {
            return JSend.error(e);
        }
    }
    
    private static void log(Request req, Response rsp) {
        StringBuilder msg = new StringBuilder();
        msg.append(String.format("%s: %s %s", req.ip(), req.requestMethod(), req.url()));
        String q = req.queryString();
        if (q != null) msg.append(String.format("?%s", q));
        log.info(msg.toString());
    }
}
