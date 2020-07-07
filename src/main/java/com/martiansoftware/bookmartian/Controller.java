package com.martiansoftware.bookmartian;

import com.martiansoftware.bookmartian.model.Bookmark;
import com.martiansoftware.bookmartian.model.Bookmartian;
import com.martiansoftware.bookmartian.model.Lurl;
import com.martiansoftware.bookmartian.model.Tag;
import com.martiansoftware.bookmartian.query.Query;
import com.martiansoftware.util.JSend;
import com.martiansoftware.util.Strings;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import io.javalin.plugin.json.JavalinJson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mlamb
 */
public class Controller {

    private static final Logger LOG = LoggerFactory.getLogger(Controller.class);
    
    private final Bookmartian _bm;
    private final Properties _appProperties;
    
    Controller(Bookmartian bm, Properties appProperties) {
        _bm = bm;
        _appProperties = appProperties;
    }
    
    void tags(Context ctx) {
        ctx.json(_bm.tags());
    }
    
    void bookmarks(Context ctx) {
        ctx.json(query(ctx));
    }
    
    void bookmark(Context ctx) {
        ctx.json(getBookmark(ctx));
    }
    
    void visit(Context ctx) throws Exception {
        String url = q(ctx, "url");
        if (url == null) throw new BadRequestResponse("a URL is required");
        try {
            assert(url != null);
            Lurl lurl = Lurl.of(url);
            Optional<Bookmark> ob = _bm.visit(lurl);
            //corsHeaders();
            if (!ob.isPresent()) throw new NotFoundResponse();  // TODO: manually respond instead of throw so we don't dump stack?
            ctx.redirect(ob.get().lurl().toString());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw(e);
        }
    }

    void backup(Context ctx) {
        // NOTE:  This is NOT a JSend result.  It's a raw JSON file provided as a download
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
        ctx.res.setContentType("application/octet-stream");
        ctx.res.setHeader("Content-disposition", "attachment; filename=" + String.format("backup-%s.bookmartian", sdf.format(new Date())));
        ctx.result(JavalinJson.toJson(new Backup(_bm)).getBytes(StandardCharsets.UTF_8));
    }

    void config(Context ctx) {
        try {
            Optional<String> configJson = _bm.config();
            if (configJson.isPresent()) {
                // return is a String of raw json, so we have to manually construct the jsend response.
                // FIXME:  IF CONFIG JSON IS INVALID, THEN THE RESPONSE WILL BE TOO                
                ctx.res.setContentType("application/json");
                ctx.result(String.format("{\n\t\"status\": \"success\",\n\t\"data\": %s\n}", configJson.get()));                
            }
            ctx.json(JSend.fail("config not found"));
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            ctx.json(JSend.error(e));
        }
    }
    
   void restore(Context ctx) {        
        String backupJson = new BufferedReader(
                                    new InputStreamReader(ctx.uploadedFile("backup").getContent(), StandardCharsets.UTF_8)
                            ).lines().collect(Collectors.joining("\n"));
        Backup backup = JavalinJson.fromJson(backupJson, Backup.class);
        
        for (Bookmark b : backup.bookmarks) _bm.update(null, b);
        for (Tag t : backup.tags) _bm.update(t);
        
        ctx.json(JSend.success(String.format("%d bookmarks, %d tags", backup.bookmarks.size(), backup.tags.size())));
    }
    
    void updateBookmark(Context ctx) {
        String url = q(ctx, "url");
        if (url == null) {
            ctx.json(JSend.fail("a URL is required"));
            return;
        }
        try {
            LOG.debug("updating bookmark: {}", url);
            String oldUrl = q(ctx, "oldUrl");
            Lurl oldLurl = (oldUrl == null) ? null : Lurl.of(oldUrl);
            if (oldLurl != null) LOG.debug("  => replacing {}", oldLurl);

            Bookmark b = Bookmark.newBuilder()
                            .url(url)
                            .title(q(ctx, "title"))
                            .imageUrl(q(ctx, "imageUrl"))
                            .notes(q(ctx, "notes"))
                            .tags(q(ctx, "tags"))
                            .build();

            //corsHeaders();
            ctx.json(JSend.success(_bm.update(oldLurl, b)));
        } catch (Exception e) {
            ctx.json(JSend.error(e));
        }
    }
   
    void about(Context ctx) {
        try {
            //corsHeaders();
            ctx.json(JSend.success(_appProperties));
        } catch (Exception e) {
            ctx.json(JSend.error(e));
        }
    }
    
    void deleteBookmark(Context ctx) {
        String url = q(ctx, "url");
        if (url == null) {
            ctx.json(JSend.fail("a URL is required"));
            return;
        }
        LOG.info("deleting bookmark: [{}]", url);
        JSend result;
        try {
            Optional<Bookmark> ob = _bm.remove(Lurl.of(url));
            //corsHeaders();
            result = ob.map(b -> JSend.success(b)).orElse(JSend.fail("no such bookmark: " + url));
        } catch (Exception e) {
            result = JSend.error(e);
        }
        ctx.json(result);
    }
    
    void importNetscapeBookmarksFile(Context ctx) {
        JSend result;
        try {
            String tags = q(ctx, "tags");
            
            Document doc = Jsoup.parse(ctx.uploadedFile("bookmarksFile").getContent(), "UTF-8", "");
            Elements links = doc.getElementsByTag("a");
            for (Element link : links) {
                LOG.info("importing {}", link.attr("HREF"));
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
                _bm.update(null, b.build());
            }
            result = JSend.success(String.format("Imported %d bookmark%s.", links.size(), links.size() == 1 ? "" : "s"));
        } catch (Exception e) {            
            result = JSend.error(e);
        }
        ctx.json(result);
    }    
 
    
    private JSend getBookmark(Context ctx) {
        String url = q(ctx, "url");
        if (url == null) return JSend.fail("a URL is required");
        try {
            Lurl lurl = Lurl.of(url);
            LOG.debug("searching for bookmark: {}", lurl);
            Optional<Bookmark> b = _bm.get(lurl);
            //corsHeaders();
            return b.map(result -> JSend.success(result)).orElse(JSend.fail("no such bookmark: " + url));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JSend.error(e);
        }
    }
    
    
    private JSend query(Context ctx) {
        try {
            //corsHeaders();
            return JSend.success(Query.of(q(ctx, "q")).execute(_bm));
        } catch (Exception e) {
            return JSend.error(e);
        }
    }

    private String q(Context ctx, String key) {
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
