package com.martiansoftware.bookmartian;

import com.martiansoftware.util.JSend;
import com.martiansoftware.bookmartian.model.Bookmark;
import com.martiansoftware.bookmartian.model.IBookmartian;
import com.martiansoftware.bookmartian.model.JsonConfig;
import com.martiansoftware.bookmartian.jsondir.JsonDirBookmartian;
import com.martiansoftware.bookmartian.model.Lurl;
import com.martiansoftware.bookmartian.model.TagNameSet;
import com.martiansoftware.bookmartian.query.Queries;
import com.martiansoftware.bookmartian.query.Query;
import java.nio.file.Paths;
import static com.martiansoftware.boom.Boom.*;
import com.martiansoftware.boom.BoomResponse;
import com.martiansoftware.boom.StatusPage;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.util.Strings;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
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
import spark.utils.IOUtils;

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
        Path bmHome = Paths.get(cmd.getString(ARG_BOOKMARTIAN_DIR));
        IBookmartian bm = JsonDirBookmartian.in(bmHome);
        
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> bm.shutdown()));
        
        before("/*", (req, rsp) -> log(req, rsp));
        before("/*", (req, rsp) -> disableCaching(req, rsp));
        
        loadPlugins(bmHome);
        
        get("/api/tags", () -> json(bm.tags()));
        
        options("/api/bookmark", () -> corsOptions());
        get("/api/bookmark", () -> getBookmark(bm));
        get("/api/bookmarks", () -> query(bm));
        get("/api/visit", () -> visit(bm));
        get("/api/query-help", () -> { response().redirect("/api/query-help.json"); return null; });
        
        options("/api/bookmark/update", () -> corsOptions());
        post("/api/bookmark/update", () -> updateBookmark(bm));
        
        post("/api/bookmark/delete", () -> deleteBookmark(bm));
        
        post("/api/bookmarks/import", () -> importNetscapeBookmarksFile(bm));
        
        get("/", (req, rsp) -> IOUtils.toString(App.class.getResourceAsStream("/static-content/index.html")));
        get("/index.html", (req, rsp) -> { rsp.redirect("/", HttpServletResponse.SC_MOVED_PERMANENTLY); return null; });
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
    
    private static void uploading() throws IOException {
        Request r = request();
        if (r.raw().getAttribute("org.eclipse.jetty.multipartConfig") == null) {
            MultipartConfigElement multipartConfigElement = new MultipartConfigElement(Files.createTempDirectory("bookmartian-").toString());
            r.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);
        }        
    }
    
    private static BoomResponse importNetscapeBookmarksFile(IBookmartian bm) {
        try {
            uploading();
            String tags = q("tags");
            Part part = request().raw().getPart("bookmarksFile");
            Document doc = Jsoup.parse(part.getInputStream(), "UTF-8", "");
            Elements links = doc.getElementsByTag("a");
            for (Element link : links) {
                log.info("Importing {}", link.attr("HREF"));
                Bookmark.Builder b = Bookmark.newBuilder()
                                        .url(link.attr("HREF"))
                                        .title(link.text())
                                        .tags(tags);
               
                String d = Strings.safeTrimToNull(link.attr("ADD_DATE"));
                if (d != null) b.created(new Date(Long.valueOf(d) * 1000));
                
                d = Strings.safeTrimToNull(link.attr("LAST_MODIFIED"));
                if (d != null) b.modified(new Date(Long.valueOf(d) * 1000));
                
                bm.replaceOrAdd(null, b.build());
            }
            return JSend.success(String.format("Imported %d bookmark%s.", links.size(), links.size() == 1 ? "" : "s"));
        } catch (Exception e) {            
            return JSend.error(e);
        }
    }
 
    private static void loadPlugins(Path bmHome) throws Exception {
        log.info("loading plugins");
        File authFile = bmHome.resolve("users").resolve("anonymous").resolve("auth.properties").toFile();
        System.setProperty("BOOKMARTIAN_AUTH_FILE", authFile.getAbsolutePath());
        
        Path pluginDir = bmHome.resolve("plugins");
        if (Files.isDirectory(pluginDir)) {
            Files.list(bmHome.resolve("plugins")).filter(p -> p.getFileName().toString().toLowerCase().endsWith(".jar")).forEach(p -> loadPlugin(p));
        } else {
            log.info("no plugins found");
        }
        log.info("finished loading plugins");
    }
    
    private static void loadPlugin(Path plugin) {
        log.info("loading {}", plugin.toAbsolutePath());
        try {
            URL[] urls = { plugin.toUri().toURL() };
            ClassLoader pcl = new URLClassLoader(urls);
            Class c = pcl.loadClass("com.martiansoftware.bookmartian.auth.AuthPlugin"); // TODO: don't hardcode class here (duh)
            log.info("creating auth plugin");
            Runnable r = (Runnable) c.newInstance();
            log.info("running auth plugin");
            r.run();
            log.info("finished loading plugins");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            System.exit(1);
        }
    }
}
