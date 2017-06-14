package com.martiansoftware.bookmartian.db;

import com.martiansoftware.bookmartian.model.User;
import com.martiansoftware.bookmartian.model.UserManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mlamb
 */
class DbUserManager implements UserManager {

    private static final Logger log = LoggerFactory.getLogger(DbUserManager.class);
    
    private final Database _db;    
    
    DbUserManager(Database db) {
        _db = db;
    }
    
    @Override
    public Optional<User> get(String username) {
        return _db.call(conn -> loadUserByName(conn, username));
    }

    @Override
    public UserManager put(User user) {
        _db.run(conn -> saveUser(conn, user));
        return this;
    }

    @Override
    public UserManager remove(User user) {
        _db.run(conn -> deleteUserByName(conn, user.username()));
        return this;
    }
    
    // -------------------------------------------------------------------------
    
    private User loadUser(ResultSet rs) throws SQLException {
        return User.newBuilder().username(rs.getString("USERNAME")).pwhash(rs.getString("PWHASH")).build();
    }
    
    private Optional<User> loadUserByName(Connection conn, String username) throws SQLException {
        try(PreparedStatement q = conn.prepareStatement("SELECT * FROM USERS WHERE USERNAME = ?;")) {                
            q.setString(1, username);
            ResultSet rs = q.executeQuery();
            if (rs.next()) return Optional.ofNullable(loadUser(rs));
        }
        return Optional.empty();
    }
    
    private void saveUser(Connection conn, User user) throws SQLException {
        try(PreparedStatement u = conn.prepareStatement("MERGE INTO USERS (USERNAME, PWHASH) VALUES (?, ?);")) {
            u.setString(1, user.username());
            u.setString(2, user.pwhash());
            u.executeUpdate(); // TODO: check return code?
        }
    }
    
    private void deleteUserByName(Connection conn, String username) throws SQLException {
        try(PreparedStatement u = conn.prepareStatement("DELETE FROM USERS WHERE USERNAME = ?;")) {
            u.setString(1, username);
            u.executeUpdate(); // TODO: check return code?
        }        
    }
}
