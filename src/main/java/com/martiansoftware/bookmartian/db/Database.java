package com.martiansoftware.bookmartian.db;

import com.martiansoftware.bookmartian.model.Bookmartian;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.martiansoftware.bookmartian.model.User;
import com.martiansoftware.bookmartian.model.UserManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author mlamb
 */
public class Database implements AutoCloseable {
    
    private static final Logger log = LoggerFactory.getLogger(DbBookmartian.class);
    private static final boolean LOG_H2 = true;
    
    private final ExecutorService _executor = Executors.newSingleThreadExecutor();    
    private final Connection _conn;

    public Database(Path p) throws ClassNotFoundException, SQLException { // creates p.mv.db
        Class.forName("org.h2.Driver");
        String dbPath = p.toFile().toURI().toString().replaceAll("/*$", ""); // drop trailing slashes from db name
        String dbUrl = String.format("jdbc:h2:%s;%s", dbPath, dbOptions());
        log.info("Opening {}", dbUrl);
        _conn = DriverManager.getConnection(dbUrl, "sa", "");
//        org.h2.tools.Console.main("-web", "-browser");
    }

    private String dbOptions() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("TRACE_LEVEL_FILE=%d;", LOG_H2 ? 4 : 0));
        sb.append("TRACE_LEVEL_SYSTEM_OUT=0;"); // no trace to stdout
        sb.append("INIT=RUNSCRIPT FROM 'classpath:com/martiansoftware/bookmartian/db/init.sql';");
        return sb.toString();        
    }
    
    @Override
    public void close() throws SQLException {
        log.error("SHUTTING DOWN DATABASE");
        Thread.dumpStack();
        _executor.shutdown();
        try {
            _executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("InterruptedException while waiting for executor to shut down.");
        }
        _conn.close();
    }
    
    public UserManager userManager() {
        return new DbUserManager(this);
    }
    
    public Bookmartian bookmartianFor(User user) {
        return new DbBookmartian(this, user);
    }
    
    // forces single-threaded database use
    <V> V call(SqlFunction<V> func) {
        Future<V> result = _executor.submit(() -> func.call(_conn));
        try {
            return result.get();
        } catch (Exception e) {
            // TODO: ROLLBACK TX
            log.error("Error executing db operation: " + e.getMessage(), e);
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new RuntimeException(e);
        }
    }
    
    // forces single-threaded database use
    void run(SqlRunnable func) {
        call(func);
    }

    @FunctionalInterface
    interface SqlFunction<V> {
        public V call(Connection conn) throws SQLException;
    }
    
    @FunctionalInterface
    interface SqlRunnable extends SqlFunction<Boolean>{
        public void run(Connection conn) throws SQLException;
        
        @Override 
        public default Boolean call(Connection conn) throws SQLException {
            run(conn);
            return true;
        }
    }
    
}
