package br.edu.ifba.inf008.plugins.ecommerce.repository;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionFactory {
    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mariadb://localhost:3306/ecommerce_inf008");
        config.setUsername("inf008");
        config.setPassword("inf008");
        config.setDriverClassName("org.mariadb.jdbc.Driver");

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        // Do not block/fail application startup if the database is unreachable yet;
        // let the failure surface as a SQLException on the first actual query instead,
        // so the JavaFX UI can catch it and show a message instead of crashing at boot.
        config.setInitializationFailTimeout(-1);

        dataSource = new HikariDataSource(config);
    }
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
