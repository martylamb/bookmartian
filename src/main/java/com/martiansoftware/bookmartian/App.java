package com.martiansoftware.bookmartian;

import com.martiansoftware.util.JSend;
import com.martiansoftware.bookmartian.model.Bookmark;
import com.martiansoftware.bookmartian.model.Json;
import com.martiansoftware.bookmartian.model.Bookmartian;
import com.martiansoftware.bookmartian.model.Lurl;
import com.martiansoftware.bookmartian.model.Tag;
import com.martiansoftware.bookmartian.mvstore.MvStoreBookmartian;
import com.martiansoftware.bookmartian.query.Query;
import java.nio.file.Paths;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.stringparsers.IntegerStringParser;
import com.martiansoftware.util.Strings;
import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.json.JavalinJson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        String defaultPort = System.getProperty("webserver.port");
        if (defaultPort == null) {
            defaultPort = "4567";
        }

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
                                .setDefault(defaultPort)
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
        
        log.info("Default charset={}", Charset.defaultCharset());
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
        
        Json.init();
        // TODO: implement a cache by username and retrieve from there on demand?
        Bookmartian bm = new MvStoreBookmartian(Paths.get(cmd.getString(JSAP_DIR)));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> bm.shutdown()));


        Javalin server = Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.requestLogger((ctx, ms) -> log(ctx, ms));
            config.addStaticFiles("/static-content", Location.CLASSPATH);
            
        }).start(cmd.contains(JSAP_PORT) ? cmd.getInt(JSAP_PORT) : 7000);

//        before("/*", (req, rsp) -> disableCaching(req, rsp));

        server.get("/api/tags", ctx -> ctx.json(bm.tags()));
        server.get("/api/bookmarks", ctx -> ctx.json(query(ctx, bm)));
        server.get("/api/bookmark", ctx -> ctx.json(getBookmark(ctx, bm)));
        server.get("/api/visit", ctx -> visit(ctx, bm));
        server.get("/api/backup", ctx -> backup(ctx, bm));
        server.get("/api/query-help",  ctx -> ctx.redirect("/api/query-help.json")); // FIXME - where is this?
        server.get("/api/config", ctx -> config(ctx, bm));
        
        Stream.of("/page/*", "/settings", "/search", "/new").forEach(
                p -> server.get(p, ctx -> ctx.redirect("/"))
        );
        
//        // example usages if the file to restore is called backup-yyyymmdd-hhmmss.json
//        // http -f post 127.0.0.1:4567/api/restore backup@backup-yyyymmdd-hhmmss.json
//        // curl --form "backup=@backup-yyyymmdd-hhmmss.json" 127.0.0.1:4567/api/restore
        server.post("/api/restore", ctx -> restore(ctx, bm));
        
        server.post("/api/bookmark/update", ctx -> updateBookmark(ctx, bm));
        server.get("/api/about", ctx -> about(ctx, bm));
        server.post("/api/bookmark/delete", ctx -> deleteBookmark(ctx, bm));
        server.post("/api/bookmarks/import", ctx-> importNetscapeBookmarksFile(ctx, bm));
    }
    

    private static void log(Context ctx, float ms) {
        StringBuilder msg = new StringBuilder();
        msg.append(String.format("%s: %s %s", ctx.req.getRemoteAddr(), ctx.req.getMethod(), ctx.req.getRequestURL()));
        String q = ctx.req.getQueryString();
        if (q != null) msg.append(String.format("?%s", q));
        log.debug(msg.toString());
    }

    private static JSend query(Context ctx, Bookmartian bm) {
        try {
            //corsHeaders();
            return JSend.success(Query.of(q(ctx, "q")).execute(bm));
        } catch (Exception e) {
            return JSend.error(e);
        }
    }

    private static JSend getBookmark(Context ctx, Bookmartian bm) {
        String url = q(ctx, "url");
        if (url == null) return JSend.fail("a URL is required");
        try {
            Lurl lurl = Lurl.of(url);
            log.debug("searching for bookmark: {}", lurl);
            Optional<Bookmark> b = bm.get(lurl);
            //corsHeaders();
            return b.map(result -> JSend.success(result)).orElse(JSend.fail("no such bookmark: " + url));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return JSend.error(e);
        }
    }
    
    private static void visit(Context ctx, Bookmartian bm) throws Exception {
        String url = q(ctx, "url");
        if (url == null) throw new BadRequestResponse("a URL is required");
        try {
            assert(url != null);
            Lurl lurl = Lurl.of(url);
            Optional<Bookmark> ob = bm.visit(lurl);
            //corsHeaders();
            if (!ob.isPresent()) throw new NotFoundResponse();  // TODO: manually respond instead of throw so we don't dump stack?
            ctx.redirect(ob.get().lurl().toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw(e);
        }
    }

    private static void backup(Context ctx, Bookmartian bm) {
        // NOTE:  This is NOT a JSend result.  It's a raw JSON file provided as a download
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
        ctx.res.setContentType("application/octet-stream");
        ctx.res.setHeader("Content-disposition", "attachment; filename=" + String.format("backup-%s.bookmartian", sdf.format(new Date())));
        ctx.result(JavalinJson.toJson(new Backup(bm)).getBytes(StandardCharsets.UTF_8));
    }
    
    private static JSend restore(Context ctx, Bookmartian bm) {
        
        String backupJson = new BufferedReader(
                                    new InputStreamReader(ctx.uploadedFile("backup").getContent(), StandardCharsets.UTF_8)
                            ).lines().collect(Collectors.joining("\n"));
        Backup backup = JavalinJson.fromJson(backupJson, Backup.class);
        
        for (Bookmark b : backup.bookmarks) bm.update(null, b);
        for (Tag t : backup.tags) bm.update(t);
        
        return JSend.success(String.format("%d bookmarks, %d tags", backup.bookmarks.size(), backup.tags.size()));
    }
    
    private static JSend updateBookmark(Context ctx, Bookmartian bm) {
        String url = q(ctx, "url");
        if (url == null) return JSend.fail("a URL is required");
        try {
            log.debug("updating bookmark: {}", url);
            String oldUrl = q(ctx, "oldUrl");
            Lurl oldLurl = (oldUrl == null) ? null : Lurl.of(oldUrl);
            if (oldLurl != null) log.debug("  => replacing {}", oldLurl);

            Bookmark b = Bookmark.newBuilder()
                            .url(url)
                            .title(q(ctx, "title"))
                            .imageUrl(q(ctx, "imageUrl"))
                            .notes(q(ctx, "notes"))
                            .tags(q(ctx, "tags"))
                            .build();

            //corsHeaders();
            return JSend.success(bm.update(oldLurl, b));
        } catch (Exception e) {
            return JSend.error(e);
        }
    }

    private static JSend about(Context ctx, Bookmartian bm) {
        try {
            //corsHeaders();
            return JSend.success(appProperties);
        } catch (Exception e) {
            return JSend.error(e);
        }
    }

    private static JSend deleteBookmark(Context ctx, Bookmartian bm) {
        String url = q(ctx, "url");
        if (url == null) return JSend.fail("a URL is required");
        log.info("deleting bookmark: [{}]", url);
        try {
            Optional<Bookmark> ob = bm.remove(Lurl.of(url));
            //corsHeaders();
            return ob.map(b -> JSend.success(b)).orElse(JSend.fail("no such bookmark: " + url));
        } catch (Exception e) {
            return JSend.error(e);
        }
    }
    
    private static JSend importNetscapeBookmarksFile(Context ctx, Bookmartian bm) {
        try {
            String tags = q(ctx, "tags");
            
            Document doc = Jsoup.parse(ctx.uploadedFile("bookmarksFile").getContent(), "UTF-8", "");
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

    private static JSend config(Context ctx, Bookmartian bm) {
        try {
            Optional<String> configJson = bm.config();
            if (configJson.isPresent()) {
                // return is a String of raw json, so we have to manually construct the jsend response.
                // FIXME:  IF CONFIG JSON IS INVALID, THEN THE RESPONSE WILL BE TOO                
                ctx.res.setContentType("application/json");
                ctx.result(String.format("{\n\t\"status\": \"success\",\n\t\"data\": %s\n}", configJson.get()));                
            }
            return JSend.fail("config not found");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return JSend.error(e);
        }
    }

    private static String q(Context ctx, String key) {
        return Strings.safeTrimToNull(ctx.req.getParameter(key));
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
