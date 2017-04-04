package com.martiansoftware.bookmartian.auth;

import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Properties;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.credentials.password.BasicSaltedSha512PasswordEncoder;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author mlamb
 */
public class SimpleAnonymousAuthenticator implements Authenticator<UsernamePasswordCredentials> {

    private static final Logger log = LoggerFactory.getLogger(SimpleAnonymousAuthenticator.class);
    
    private final Path _authFile;
    
    public SimpleAnonymousAuthenticator(Path authFile) {
        _authFile = authFile;
        if (!Files.exists(authFile)) createAuthFile(_authFile);
    }
    
    private boolean auth(UsernamePasswordCredentials c) throws CredentialsException {
        log.info("loading auth file {}", _authFile);
        try (InputStream fin = Files.newInputStream(_authFile)) {
            Properties props = new Properties();
            props.load(fin);
            log.info("loaded credentials file");
            String salt = props.getProperty("salt");
            String pwhash = props.getProperty("pwhash");
            BasicSaltedSha512PasswordEncoder encoder = new BasicSaltedSha512PasswordEncoder(salt);
            log.info("checking credentials: [{}], [{}]. [{}]", salt, pwhash, c.getPassword());
            boolean result = encoder.matches(c.getPassword(), pwhash);
            log.info("result = {}", result);
            return result;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new CredentialsException(e);
        }
    }
    
    @Override
    public void validate(UsernamePasswordCredentials c, WebContext wc) throws HttpAction {
        if (c == null) throw new CredentialsException("no credentials");
        if (auth(c)) {
            CommonProfile p = new CommonProfile();
            p.setId(c.getUsername());
            p.addAttribute(Pac4jConstants.USERNAME, c.getUsername());
            c.setUserProfile(p);
        } else {
            throw new CredentialsException("invalid username or password");
        }
    }
    
    private void createAuthFile(Path authFile) {
        char[] pw1 = null, pw2 = null;
        try {
            log.info("Creating auth file {}", authFile);
            Console console = System.console();
            if (console == null) log.error("unable to obtain console");
            boolean done = false;
            while (!done) {
                console.printf("\nEnter new passphrase: ");
                pw1 = console.readPassword();
                console.printf("  Confirm passphrase: ");
                pw2 = console.readPassword();
                if (Arrays.equals(pw1, pw2)) {
                    done = true;
                } else {
                    console.printf("PASSPHRASES DO NOT MATCH\n");
                }
            }

            byte[] saltBytes = new byte[512/8];
            SecureRandom r = new SecureRandom();
            r.nextBytes(saltBytes);
            StringBuilder saltbuf = new StringBuilder();
            for (byte b : saltBytes) saltbuf.append(String.format("%02x", b));
            String salt = saltbuf.toString();

            BasicSaltedSha512PasswordEncoder encoder = new BasicSaltedSha512PasswordEncoder(salt);

            Properties authProps = new Properties();        
            authProps.setProperty("salt", salt);        
            authProps.setProperty("pwhash", encoder.encode(new String(pw1)));

            try (OutputStream fout = Files.newOutputStream(authFile)) {
                authProps.store(fout, null);
            }

            log.info("Saved new credentials to {}", authFile);
        } catch (IOException e) {
            log.error("Unable to save auth file {}: {}", _authFile, e.getMessage(), e);
        } finally {
            zeroize(pw1);
            zeroize(pw2);
        }
    }
    
    private void zeroize(char[] c) {
        for (int i = 0; i < c.length; ++i) c[i] = '\0';
    }
    
}
