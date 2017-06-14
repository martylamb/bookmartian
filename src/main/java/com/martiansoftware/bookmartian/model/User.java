package com.martiansoftware.bookmartian.model;

import com.martiansoftware.util.PasswordStorage;
import com.martiansoftware.util.PasswordStorage.CannotPerformOperationException;
import com.martiansoftware.validation.Hope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mlamb
 */
public class User {

    public static final User ANONYMOUS =
        new User("anonymous", "THIS_IS_IGNORED") {
            @Override public String pwhash() { return ""; }
            @Override public boolean authenticate(String pw) { return true; }
        };
    
    private final String _username, _pwhash;
    
    private static final Logger log = LoggerFactory.getLogger(User.class);
    
    private User(String username, String pwhash) {
        _username = Hope.that(username).named("username")
                        .isNotNull()
                        .map(s -> s.trim().toLowerCase())
                        .isNotNullOrEmpty()
                        .value();
        
        _pwhash = Hope.that(pwhash).named("password hash").isNotNullOrEmpty().value();        
    }
    
    public String username() { return _username; }
    public String pwhash() { return _pwhash; }
    public boolean authenticate(String pw) {
        if (pw == null) return false;
        try {
            return PasswordStorage.verifyPassword(pw, pwhash());
        } catch (Exception e) {
            log.error("Unable to verify password for " + username() + ": " + e.getMessage(), e);
            return false;
        }
    }
    
    public static Builder newBuilder() { return new Builder(); }
    public Builder toBuilder() { return new Builder(this); }
    
    public static class Builder {
        private String _username, _pwhash;
        private Builder() {}
        private Builder(User user) {
            username(user.username()).pwhash(user.pwhash());
        }
        
        public Builder username(String username) { _username = username; return this; }
        public Builder pwhash(String pwhash) { _pwhash = pwhash; return this; }
        public Builder password(String password) {
            try {
                _pwhash = PasswordStorage.createHash(password);
            } catch (CannotPerformOperationException e) {
                log.error("Unable to set password for " + _username + ": " + e.getMessage(), e);
                throw new RuntimeException(e);
            }
            return this;
        }
        public User build() {
            return new User(_username, _pwhash);
        }        
    }
    
}
