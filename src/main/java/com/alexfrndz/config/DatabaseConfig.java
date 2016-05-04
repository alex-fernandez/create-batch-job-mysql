package com.alexfrndz.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;
import javax.sql.DataSource;
import java.beans.PropertyVetoException;

@Configuration
@Slf4j
public class DatabaseConfig {
    private static final String PROPERTY_NAME_DATABASE_DRIVER = "db.driver";

    private static final String PROPERTY_NAME_DATABASE_NAME = "db.name";

    private static final String PROPERTY_NAME_DATABASE_PASSWORD = "db.password";

    private static final String PROPERTY_NAME_DATABASE_URL = "db.url";

    private static final String PROPERTY_NAME_DATABASE_USERNAME = "db.username";

    private static final String DB_CACHE_PREP_STMTS = "db.cachePrepStmts";

    private static final String DB_PREP_STMTS_CACHE_SIZE = "db.prepStmtCacheSize";

    private static final String DB_PREP_STMTS_CACHE_SQL_LIMIT = "db.prepStmtCacheSqlLimit";

    private static final String DB_USE_SERVER_PREP_STMTS = "db.useServerPrepStmts";

    private static final String CONNECTION_POOL_NAME = "apiv4";

    @Resource
    private Environment environment;

    @Bean
    public HikariConfig hikariConfig() {
        HikariConfig config = new HikariConfig();
       // config.setRegisterMbeans(true);
        config.setPoolName(CONNECTION_POOL_NAME);
        config.setDataSourceClassName(environment.getProperty(PROPERTY_NAME_DATABASE_DRIVER));
        String url = environment.getProperty(PROPERTY_NAME_DATABASE_URL);
        config.addDataSourceProperty("url", url);
        config.addDataSourceProperty("user", environment.getProperty(PROPERTY_NAME_DATABASE_USERNAME));
        config.addDataSourceProperty("password", environment.getProperty(PROPERTY_NAME_DATABASE_PASSWORD));
        log.debug("Connecting to DB at {}", url);

        return config;
    }

    @Bean(destroyMethod = "close")
    @Qualifier("mysql.datasource")
    public DataSource dataSource(HikariConfig config) throws PropertyVetoException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException {
        log.debug("Configuring Datasource");
        if (environment.getProperty(PROPERTY_NAME_DATABASE_URL) == null && environment.getProperty(PROPERTY_NAME_DATABASE_NAME) == null) {
            log.error("Your database connection pool configuration is incorrect! The application" +
                    "cannot start.");
            throw new ApplicationContextException("Database connection pool is not configured correctly");
        }

        config.addDataSourceProperty("cachePrepStmts", environment.getProperty(DB_CACHE_PREP_STMTS, "true"));
        config.addDataSourceProperty("prepStmtCacheSize", environment.getProperty(DB_PREP_STMTS_CACHE_SIZE, "250"));
        config.addDataSourceProperty("prepStmtCacheSqlLimit", environment.getProperty(DB_PREP_STMTS_CACHE_SQL_LIMIT, "2048"));
        config.addDataSourceProperty("useServerPrepStmts", environment.getProperty(DB_USE_SERVER_PREP_STMTS, "true"));
        HikariDataSource hikariDataSource = new HikariDataSource(config);
        hikariDataSource.setConnectionTimeout(90000);

        return hikariDataSource;

    }

}
