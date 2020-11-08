package ru.javaops.masterjava.service.mail;

import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.logging.SLF4JLog;
import org.skife.jdbi.v2.tweak.ConnectionFactory;
import ru.javaops.masterjava.config.Configs;
import ru.javaops.masterjava.service.mail.dao.AbstractDao;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.DriverManager;

@Slf4j
public class DBIProvider {

    private volatile static ConnectionFactory connectionFactory = null;

    private static class DBIHolder {
        static final DBI jDBI;

        static {
            DBI dbi;
            if (connectionFactory != null) {
                log.info("Init jDBI with  connectionFactory");
                dbi = new DBI(connectionFactory);
            } else {
                try {
                    log.info("Init jDBI with  JNDI");
                    InitialContext ctx = new InitialContext();
                    dbi = new DBI((DataSource) ctx.lookup("java:/comp/env/jdbc/masterjava"));
                } catch (Exception ex) {
                    try {
                        Config db = Configs.getConfig("persist.conf","db");
                        try {
                            Class.forName("org.postgresql.Driver");
                        } catch (ClassNotFoundException e) {
                            throw new IllegalStateException("PostgreSQL driver not found", e);
                        }
                        init(()-> DriverManager.getConnection(db.getString("url"), db.getString("user"), db.getString("password")));
                        dbi = new DBI(connectionFactory);
                    } catch (Exception e) {
                        throw new IllegalStateException("PostgreSQL initialization failed", e);
                    }
                }
            }
            jDBI = dbi;
            jDBI.setSQLLog(new SLF4JLog());
        }
    }

    public static void init(ConnectionFactory connectionFactory) {
        DBIProvider.connectionFactory = connectionFactory;
    }

    public static DBI getDBI() {
        return DBIHolder.jDBI;
    }

    public static <T extends AbstractDao> T getDao(Class<T> daoClass) {
        return DBIHolder.jDBI.onDemand(daoClass);
    }
}
