package com.martiansoftware.bookmartian;

import com.martiansoftware.bookmartian.model.Json;
import com.martiansoftware.bookmartian.model.Bookmartian;
import com.martiansoftware.bookmartian.mvstore.MvStoreBookmartian;
import java.nio.file.Paths;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.stringparsers.IntegerStringParser;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.Charset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Properties;
import java.util.stream.Stream;

/**
 *
 * @author mlamb
 */
public class App {
       
    private static final Logger LOG = LoggerFactory.getLogger(App.class);
    private static final String JSAP_DIR = "bookmartian-dir";
    private static final String JSAP_PORT = "bookmartian-port";
    private static final Properties APP_PROPERTIES = new java.util.Properties();
    
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
            APP_PROPERTIES.getProperty("project.name"),
            APP_PROPERTIES.getProperty("project.version"),
            APP_PROPERTIES.getProperty("git.commit.id.describe-short"));
        
        System.out.format("%s%n%n", s);
        LOG.info(s);
        
        LOG.info("Default charset={}", Charset.defaultCharset());
    }
    
    public static void main(String[] args) throws Exception {
        APP_PROPERTIES.load(App.class.getResourceAsStream("/bookmartian.properties"));
        
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

        Controller controller = new Controller(bm, APP_PROPERTIES);

        Javalin server = Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.requestLogger((ctx, ms) -> log(ctx, ms));
            config.addStaticFiles("/static-content", Location.CLASSPATH);
            
        }).start(cmd.contains(JSAP_PORT) ? cmd.getInt(JSAP_PORT) : 7000);

        server.get("/api/tags", ctx -> controller.tags(ctx));
        server.get("/api/bookmarks", ctx -> controller.bookmarks(ctx));
        server.get("/api/bookmark", ctx -> controller.bookmark(ctx));
        server.get("/api/visit", ctx -> controller.visit(ctx));
        server.get("/api/backup", ctx -> controller.backup(ctx));
        server.get("/api/config", ctx -> controller.config(ctx));
        
        Stream.of("/page/*", "/settings", "/search", "/new").forEach(
                p -> server.get(p, ctx -> ctx.redirect("/"))
        );
        
//        // example usages if the file to restore is called backup-yyyymmdd-hhmmss.json
//        // http -f post 127.0.0.1:4567/api/restore backup@backup-yyyymmdd-hhmmss.json
//        // curl --form "backup=@backup-yyyymmdd-hhmmss.json" 127.0.0.1:4567/api/restore
        server.post("/api/restore", ctx -> controller.restore(ctx));
        
        server.post("/api/bookmark/update", ctx -> controller.updateBookmark(ctx));
        server.get("/api/about", ctx -> controller.about(ctx));
        server.post("/api/bookmark/delete", ctx -> controller.deleteBookmark(ctx));
        server.post("/api/bookmarks/import", ctx-> controller.importNetscapeBookmarksFile(ctx));
    }

    private static void log(Context ctx, float ms) {
        StringBuilder msg = new StringBuilder();
        msg.append(String.format("%s: %s %s", ctx.req.getRemoteAddr(), ctx.req.getMethod(), ctx.req.getRequestURL()));
        String q = ctx.req.getQueryString();
        if (q != null) msg.append(String.format("?%s", q));
        LOG.debug(msg.toString());
    }
}
