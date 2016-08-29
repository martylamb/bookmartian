package com.martiansoftware.bookmartian;

import java.nio.file.Paths;
import static com.martiansoftware.boom.Boom.*;
import com.martiansoftware.boom.BoomResponse;
import com.martiansoftware.util.Strings;
import java.io.File;
import java.io.IOException;
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
    
    private static Logger log = LoggerFactory.getLogger(App.class);
    
    public static void main(String[] args) throws Exception {
        if (args.length !=1) {
            System.err.println("Usage: bookmartian BOOKMARKS_FILE");
            System.exit(1);
        }
        
        JsonConfig.init();
        
        BookmarkCollection bc = new BookmarkCollection(Paths.get(args[0]));
        
        before("/*", (req, rsp) -> log(req,rsp));
        get("/api/tags", () -> json(bc.tags()));        
        get("/api/bookmark", () -> getBookmark(bc));
        get("/api/bookmarks", () -> json(bc.bookmarks(request().queryParams("tags"))));
        post("/api/bookmark/update", () -> updateBookmark(bc));
        post("/api/bookmark/delete", () -> deleteBookmark(bc));
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
//        new Exception().printStackTrace();
        StringBuilder msg = new StringBuilder();
        msg.append(String.format("%s: %s %s", req.ip(), req.requestMethod(), req.url()));
        String q = req.queryString();
        if (q != null) msg.append(String.format("?%s", q));
        log.info(msg.toString());
    }
}
