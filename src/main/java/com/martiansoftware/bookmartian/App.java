package com.martiansoftware.bookmartian;

import com.martiansoftware.bookmartian.auth.FormAuthenticator;
import com.martiansoftware.bookmartian.journal.JournalBookmartian;
import com.martiansoftware.util.JSend;
import com.martiansoftware.bookmartian.model.Bookmark;
import com.martiansoftware.bookmartian.model.JsonConfig;
import com.martiansoftware.bookmartian.model.Bookmartian;
import com.martiansoftware.bookmartian.model.Lurl;
import com.martiansoftware.bookmartian.model.Tag;
import com.martiansoftware.bookmartian.query.Query;
import java.nio.file.Paths;
import static com.martiansoftware.boom.Boom.*;
import com.martiansoftware.boom.BoomResponse;
import com.martiansoftware.boom.Json;
import com.martiansoftware.boom.MimeType;
import com.martiansoftware.boom.StatusPage;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.stringparsers.IntegerStringParser;
import com.martiansoftware.util.Strings;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import javax.servlet.MultipartConfigElement;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import static spark.Spark.port;
import spark.utils.IOUtils;
import java.util.Optional;
import java.util.Properties;

/**
 *
 * @author mlamb
 */
public class App {
       
    private static Logger log = LoggerFactory.getLogger(App.class);
    private static final String JSAP_DIR = "bookmartian-dir";
    private static final String JSAP_PORT = "bookmartian-port";
    private static Properties appProperties = new java.util.Properties();
    
    private static JSAP jsap() throws JSAPException {
        JSAP jsap = new JSAP();

        String defaultDir = System.getProperty("user.dir");
        jsap.registerParameter(new FlaggedOption(JSAP_DIR)
                                .setShortFlag('d')
                                .setLongFlag("dir")
                                .setDefault(defaultDir)
                                .setRequired(true)
                                .setHelp("specifies the bookmartian data directory (defaults to current directory: " + defaultDir + ")")
        );
        
        jsap.registerParameter(new FlaggedOption(JSAP_PORT)
                                .setShortFlag('p')
                                .setLongFlag("port")
                                .setStringParser(new IntegerStringParser())
                                .setDefault("4567")
                                .setRequired(false)
                                .setHelp("specifies the port on which the webserver should listen")
        );
                
        return jsap;
    }

    private static void banner() {
        new LineNumberReader(new InputStreamReader(App.class.getResourceAsStream("/bookmartian.header")))
            .lines()
            .filter(s -> !s.startsWith("#"))
            .forEach(s -> System.out.println(s));

        String s = String.format("%s v%s (%s)%n",
            appProperties.getProperty("project.name"),
            appProperties.getProperty("project.version"),
            appProperties.getProperty("git.commit.id.describe-short"));
        
        System.out.format("%s%n%n", s);
        log.info(s);        
    }
    
    public static void main(String[] args) throws Exception {

        appProperties.load(App.class.getResourceAsStream("/bookmartian.properties"));
        
        JSAP jsap = jsap();
        JSAPResult cmd = jsap().parse(args);
        if (!cmd.success()) {
            System.err.format("Usage: bookmartian %s%n", jsap.getUsage());
            System.exit(1);
        }

        banner();
        
        if (cmd.contains(JSAP_PORT)) {
            port(cmd.getInt(JSAP_PORT));
        }
        JsonConfig.init();
        // TODO: implement a cache by username and retrieve from there on demand
        Bookmartian bm = new JournalBookmartian(Paths.get(cmd.getString(JSAP_DIR)).resolve("userdata.bookmartian"));

// TODO        Runtime.getRuntime().addShutdownHook(new Thread(() -> bm.shutdown()));
        
        before("/*", (req, rsp) -> log(req, rsp));
        before("/*", (req, rsp) -> disableCaching(req, rsp));
        
// TODO       if (cmd.getBoolean(JSAP_REQUIRELOGIN)) {
//            Spark.awaitInitialization();
//            initAuth(bmHome);
//        }
        
        get("/api/tags", () -> json(bm.tags()));
        
        options("/api/bookmark", () -> corsOptions());
        get("/api/bookmark", () -> getBookmark(bm));
        get("/api/bookmarks", () -> query(bm));
        get("/api/visit", () -> visit(bm));
        get("/api/query-help", () -> { response().redirect("/api/query-help.json"); return null; });
        get("/api/backup", () -> backup(bm));
        
        // example usages:
        // http -f post 127.0.0.1:4567/api/restore backup@backup.json
        // curl --form "backup=@backup.json" 127.0.0.1:4567/api/restore
        post("/api/restore", () -> restore(bm));
        
        options("/api/bookmark/update", () -> corsOptions());
        post("/api/bookmark/update", () -> updateBookmark(bm));
        
        post("/api/bookmark/delete", () -> deleteBookmark(bm));
        
        post("/api/bookmarks/import", () -> importNetscapeBookmarksFile(bm));
        
        get("/api/about", () -> JSend.success(appProperties));
        
        get("/", (req, rsp) -> IOUtils.toString(App.class.getResourceAsStream("/static-content/index.html")));
        get("/index.html", (req, rsp) -> {             
            String q = req.raw().getQueryString();
            String dest = String.format("/%s%s", Strings.isEmpty(q) ? "" : "?", Strings.isEmpty(q) ? "" : q);
            rsp.redirect(dest, HttpServletResponse.SC_MOVED_PERMANENTLY); return null; 
        });
    }
    
    private static BoomResponse query(Bookmartian bm) {
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
    
    private static BoomResponse updateBookmark(Bookmartian bm) {
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
            return JSend.success(bm.update(oldLurl, b));
        } catch (Exception e) {
            return JSend.error(e);
        }
    }
    
    private static BoomResponse getBookmark(Bookmartian bm) {
        String url = q("url");
        if (url == null) return JSend.fail("a URL is required");
        try {
            Lurl lurl = Lurl.of(url);
            log.debug("searching for bookmark: {}", lurl);
            Optional<Bookmark> b = bm.get(lurl);
            corsHeaders();
            return b.map(result -> JSend.success(result)).orElse(JSend.fail("no such bookmark: " + url));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return JSend.error(e);
        }
    }
    
    private static BoomResponse backup(Bookmartian bm) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
        
        return new BoomResponse(Json.toJson(new Backup(bm)))
                        .as(MimeType.BIN)
                        .named(String.format("backup-%s.bookmartian", sdf.format(new Date())));
    }
    
    private static BoomResponse restore(Bookmartian bm) {
        try {
            uploading();
            Part part = request().raw().getPart("backup");
            Backup backup = Json.fromJson(new InputStreamReader(part.getInputStream()), Backup.class);
            
            for (Bookmark b : backup.bookmarks) bm.update(null, b);
            for (Tag t : backup.tags) bm.update(t);
            
            return JSend.success(String.format("%d bookmarks, %d tags", backup.bookmarks.size(), backup.tags.size()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return StatusPage.of(e);
        }
    }
    
    private static BoomResponse visit(Bookmartian bm) {
        String url = q("url");
        if (url == null) halt(400, "a URL is required");
        try {
            assert(url != null);
            Lurl lurl = Lurl.of(url);
            Optional<Bookmark> ob = bm.visit(lurl);
            if (!ob.isPresent()) return StatusPage.of(404, "Not Found");
            response().redirect(ob.get().lurl().toString());
            return null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return StatusPage.of(e);
        }
    }
    
    private static BoomResponse deleteBookmark(Bookmartian bm) {
        String url = q("url");
        if (url == null) return JSend.fail("a URL is required");
        log.info("deleting bookmark: [{}]", url);
        try {
            Optional<Bookmark> ob = bm.remove(Lurl.of(url));
            return ob.map(b -> JSend.success(b)).orElse(JSend.fail("no such bookmark: " + url));
        } catch (Exception e) {
            return JSend.error(e);
        }
    }
    
    private static void log(Request req, Response rsp) {
        StringBuilder msg = new StringBuilder();
        msg.append(String.format("%s: %s %s", req.ip(), req.requestMethod(), req.url()));
        String q = req.queryString();
        if (q != null) msg.append(String.format("?%s", q));
        log.debug(msg.toString());
    }
    
    private static void uploading() throws IOException {
        Request r = request();
        if (r.raw().getAttribute("org.eclipse.jetty.multipartConfig") == null) {
            MultipartConfigElement multipartConfigElement = new MultipartConfigElement(Files.createTempDirectory("bookmartian-").toString());
            r.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);
        }        
    }
    
    private static BoomResponse importNetscapeBookmarksFile(Bookmartian bm) {
        try {
            uploading();
            String tags = q("tags");
            Part part = request().raw().getPart("bookmarksFile");
            Document doc = Jsoup.parse(part.getInputStream(), "UTF-8", "");
            Elements links = doc.getElementsByTag("a");
            for (Element link : links) {
                log.info("importing {}", link.attr("HREF"));
                Bookmark.Builder b = Bookmark.newBuilder()
                                        .url(link.attr("HREF"))
                                        .title(link.text())
                                        .tags(tags);
               
                String d = Strings.safeTrimToNull(link.attr("ADD_DATE"));
                if (d != null) b.created(new Date(Long.valueOf(d) * 1000));
                
                d = Strings.safeTrimToNull(link.attr("LAST_MODIFIED"));
                if (d != null) b.modified(new Date(Long.valueOf(d) * 1000));
                
                d = Strings.safeTrimToNull(link.attr("TAGS"));                
                if (d != null) b.tags(d.replaceAll(",", " ").replaceAll("[^a-zA-Z0-9_. -]", "_"));
                bm.update(null, b.build());
            }
            return JSend.success(String.format("Imported %d bookmark%s.", links.size(), links.size() == 1 ? "" : "s"));
        } catch (Exception e) {            
            return JSend.error(e);
        }
    }
 
    private static void initAuth(Path bmHome) throws Exception {
        final FormAuthenticator fa = FormAuthenticator
                                        .newBuilder()
                                        .authFile(bmHome.resolve("users").resolve("anonymous").resolve("auth.properties"))
                                        .build();
     	fa.secure("/");
     	fa.secure("/index.html");
     	fa.secure("/api/*");
    }
        
    private static class Backup {        
        private final Collection<Tag> tags;
        private final Collection<Bookmark> bookmarks;
        
        public Backup(Bookmartian bm) {
            tags = bm.tags();
            bookmarks = bm.bookmarks();
        }
    }
}
