package com.martiansoftware.bookmartian;

import com.martiansoftware.util.JSend;
import com.martiansoftware.bookmartian.model.Bookmark;
import com.martiansoftware.bookmartian.model.IBookmartian;
import com.martiansoftware.bookmartian.model.JsonConfig;
import com.martiansoftware.bookmartian.jsondir.JsonDirBookmartian;
import com.martiansoftware.bookmartian.model.Lurl;
import com.martiansoftware.bookmartian.model.Query;
import java.nio.file.Paths;
import static com.martiansoftware.boom.Boom.*;
import com.martiansoftware.boom.BoomResponse;
import com.martiansoftware.boom.StatusPage;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

/**
 *
 * @author mlamb
 */
public class App {
    
    private static final String ARG_BOOKMARTIAN_DIR = "BOOKMARTIAN_DIR";
    private static Logger log = LoggerFactory.getLogger(App.class);

    private static JSAP jsap() throws JSAPException {
        JSAP jsap = new JSAP();

        jsap.registerParameter(new FlaggedOption(ARG_BOOKMARTIAN_DIR)
                                .setShortFlag('d')
                                .setLongFlag("dir")
                                .setDefault(System.getProperty("user.dir"))
                                .setRequired(true));
        return jsap;
    }

    public static void main(String[] args) throws Exception {
        
        JSAP jsap = jsap();
        JSAPResult cmd = jsap().parse(args);
        if (!cmd.success()) {
            System.err.format("Usage: bookmartian %s", jsap.getUsage());
            System.exit(1);
        }
        
        JsonConfig.init();
        IBookmartian bm = JsonDirBookmartian.in(Paths.get(cmd.getString(ARG_BOOKMARTIAN_DIR)));
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> bm.shutdown()));
        
        before("/*", (req, rsp) -> log(req, rsp));
        before("/*", (req, rsp) -> disableCaching(req, rsp));
        
        get("/api/tags", () -> json(bm.tags()));
        
        options("/api/bookmark", () -> corsOptions());
        get("/api/bookmark", () -> getBookmark(bm));
        get("/api/bookmarks", () -> query(bm));
        get("/api/visit", () -> visit(bm));
        
        options("/api/bookmark/update", () -> corsOptions());
        post("/api/bookmark/update", () -> updateBookmark(bm));
        
        post("/api/bookmark/delete", () -> deleteBookmark(bm));
    }
    
    private static BoomResponse query(IBookmartian bm) {
        try {
            return JSend.success(Query.of(q("q")).execute(bm));
        } catch (Exception e) {
            return JSend.error(e);
        }
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
    
    private static BoomResponse updateBookmark(IBookmartian bm) {
        String url = q("url");
        if (url == null) return JSend.fail("a URL is required");
        try {
            log.debug("updating bookmark: {}", url);
            String oldUrl = q("oldUrl");
            Lurl oldLurl = (oldUrl == null) ? null : Lurl.of(oldUrl);
            if (oldLurl != null) log.debug("  => replacing {}", oldLurl);

            Bookmark b = Bookmark.newBuilder()
                            .url(url)
                            .title(q("title"))
                            .imageUrl(q("imageUrl"))
                            .notes(q("notes"))
                            .tags(q("tags"))
                            .build();

            corsHeaders();
            return JSend.success(bm.replaceOrAdd(oldLurl, b));
        } catch (Exception e) {
            return JSend.error(e);
        }
    }
    
    private static BoomResponse getBookmark(IBookmartian bm) {
        String url = q("url");
        if (url == null) return JSend.fail("a URL is required");
        try {
            Lurl lurl = Lurl.of(url);
            log.debug("searching for bookmark: {}", lurl);
            Bookmark b = bm.get(lurl);
            corsHeaders();
            return (b == null) ? JSend.fail("no such bookmark: " + url) : JSend.success(b);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return JSend.error(e);
        }
    }
    
    private static BoomResponse visit(IBookmartian bm) {
        String url = q("url");
        if (url == null) halt(400, "a URL is required");
        try {
            assert(url != null);
            Lurl lurl = Lurl.of(url);
            Bookmark b = bm.visit(lurl);
            if (b == null) return StatusPage.of(404, "Not Found");
            response().redirect(b.lurl().toString());
            return null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return StatusPage.of(e);
        }
    }
    
    private static BoomResponse deleteBookmark(IBookmartian bm) {
        String url = q("url");
        if (url == null) return JSend.fail("a URL is required");
        log.warn("deleting bookmark: {}", url);
        try {
            Bookmark b = bm.remove(Lurl.of(url));
            return (b == null) ? JSend.fail("no such bookmark: " + url) : JSend.success(b);
        } catch (Exception e) {
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
