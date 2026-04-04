package com.mylibrary.actuator;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.TimeUnit;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private String databaseName = "unknown";
    private String databaseVersion = "unknown";

    public DatabaseHealthIndicator(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void init() {
        try (Connection conn = dataSource.getConnection()) {
            databaseName = conn.getMetaData().getDatabaseProductName();
            databaseVersion = conn.getMetaData().getDatabaseProductVersion();
        } catch (Exception e) {
            databaseName = "unknown";
            databaseVersion = "unknown";
        }
    }

    @Override
    public Health health() {
        try {
            long start = System.nanoTime();
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            long responseTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            return Health.up()
                    .withDetail("databaseName", databaseName)
                    .withDetail("databaseVersion", databaseVersion)
                    .withDetail("responseTimeMs", responseTime)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", "DOWN")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
