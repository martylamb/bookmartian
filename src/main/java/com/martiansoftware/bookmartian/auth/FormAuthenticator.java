package com.martiansoftware.bookmartian.auth;

import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigFactory;
import org.pac4j.http.client.indirect.FormClient;
import org.pac4j.sparkjava.ApplicationLogoutRoute;
import org.pac4j.sparkjava.CallbackRoute;
import org.pac4j.sparkjava.DefaultHttpActionAdapter;
import org.pac4j.sparkjava.SecurityFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Route;
import spark.Spark;
import static spark.Spark.*;

/**
 * placeholder authenticator; right now only designed to put a password on the
 * single account that bookmartian supports
 * 
 * @author mlamb
 */
public class FormAuthenticator implements ConfigFactory {

    private static final Logger log = LoggerFactory.getLogger(FormAuthenticator.class);
    
    private final String _loginFormAt;
    private final String _loginPostAt;
    private final String _logoutAt;
    private final String _afterLogoutGoTo;
    private final Route _renderLoginFormWith;
    private final Config _config;
    private final Path _authFile;
    
    public FormAuthenticator(String loginFormAt, Route renderLoginFormWith, String loginPostAt, String logoutAt, String afterLogoutGoTo, Path authFile) {
        _loginFormAt = loginFormAt;
        if (renderLoginFormWith == null) {
            _renderLoginFormWith = (req, rsp) -> {
                return "<form action=\"" + loginPostAt() + "\" method=\"POST\">\n"
                        + "<input type=\"text\" name=\"username\" value=\"\" />\n"
                        + "<p/>\n"
                        + "<input type=\"password\" name=\"password\" value=\"\" />\n"
                        + "<p />\n"
                        + "<input type=\"submit\" name=\"submit\" value=\"Submit\" />\n"
                        + "</form>";                
            };
        } else {
            _renderLoginFormWith = renderLoginFormWith;
        }
        if (_renderLoginFormWith == null) throw new NullPointerException("a login form route is required");
        _loginPostAt = loginPostAt;
        _logoutAt = logoutAt;
        _afterLogoutGoTo = afterLogoutGoTo;
        _authFile = authFile;
        _config = initConfig();
    }

    public final String loginFormAt() { return _loginFormAt; }
    public final Route renderLoginFormWith() { return _renderLoginFormWith; }
    public final String loginPostAt() {
        return _config.getClients().findClient(FormClient.class).getCallbackUrl();
    }
    public final String logoutAt() { return _logoutAt; }
    public final String afterLogoutGoTo() { return _afterLogoutGoTo; }

    public final String secure(String sparkPath) {
        before(sparkPath, new SecurityFilter(_config, "FormClient"));
        return sparkPath;
    }
    
    @Override
    public Config build() {
        return _config;
    }
    
    private Config initConfig() {
        final FormClient formClient = new FormClient(loginFormAt(), new SimpleAnonymousAuthenticator(_authFile));
        final Clients clients = new Clients(_loginPostAt, formClient);
        final Config config = new Config(clients);
        config.setHttpActionAdapter(new DefaultHttpActionAdapter());

        // register callback for all http methods
        final CallbackRoute callback = new CallbackRoute(config, null, true);
        Stream<BiConsumer<String, Route>> methods = Stream.of(Spark::get,
                                                                Spark::post, 
                                                                Spark::put, 
                                                                Spark::delete, 
                                                                Spark::head, 
                                                                Spark::trace, 
                                                                Spark::connect, 
                                                                Spark::options);
        methods.forEach(m -> m.accept(_loginPostAt, callback));

        // provide the config a route for the login?
        get(loginFormAt(), renderLoginFormWith());
        
        // what do we do once we log out?
        ApplicationLogoutRoute alr = new ApplicationLogoutRoute(config, afterLogoutGoTo(), ".*");
        get(logoutAt(), alr);

        return config;
    }
    
    public static class Builder {
        private String _loginFormAt = "/login";
        private String _loginPostAt = "/loginComplete";
        private String _logoutAt = "/logout";
        private String _afterLogoutGoTo = "/";
        private Path _authFile;
        private Route _renderLoginFormWith;
        
        public Builder loginFormAt(String loginFormPath) {
            _loginFormAt = scrubPath(loginFormPath);
            return this;
        }
        
        public Builder loginPostAt(String loginPostPath) {
            _loginPostAt = scrubPath(loginPostPath);
            return this;
        }
        
        public Builder logoutAt(String logoutPath) {
            _logoutAt = scrubPath(logoutPath);
            return this;
        }
        
        public Builder afterLogoutGoTo(String afterLogoutPathOrUrl) {
            _afterLogoutGoTo = scrubPath(afterLogoutPathOrUrl);
            return this;
        }
        
        public Builder renderLoginFormWith(Route loginFormRoute) {
            _renderLoginFormWith = loginFormRoute;
            return this;
        }
        
        private String scrubPath(String s) {
            return s;
//            Pattern p = Pattern.compile("^[\\s/]*(.*[^\\s/])[\\s/]*$");
//            Matcher m = p.matcher(s);
//            if (!m.matches()) throw new IllegalArgumentException("Invalid path: " + s);
//            return "/" + m.group(0);
        }

        public Builder authFile(Path authFile) {
            _authFile = authFile;
            return this;
        }
        
        public FormAuthenticator build() {
            return new FormAuthenticator(_loginFormAt, _renderLoginFormWith, _loginPostAt, _logoutAt, _afterLogoutGoTo, _authFile);
        }
    }
    
    public static Builder newBuilder() {
        return new Builder();
    }
}
