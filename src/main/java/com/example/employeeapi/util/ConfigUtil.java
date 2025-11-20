package com.example.employeeapi.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ConfigUtil loads and provides access to application configuration properties.
 */
public class ConfigUtil {
    private static final Properties properties = new Properties();
    private static final String CONFIG_FILE = "application.properties";

    static {
        try (InputStream input = ConfigUtil.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                System.err.println("Unable to find " + CONFIG_FILE + ", using defaults");
            } else {
                properties.load(input);
                System.out.println("Configuration loaded from " + CONFIG_FILE);
            }
        } catch (IOException e) {
            System.err.println("Error loading configuration: " + e.getMessage());
        }
    }

    /**
     * Get server port
     */
    public static int getServerPort() {
        return Integer.parseInt(properties.getProperty("server.port", "8080"));
    }

    /**
     * Get database URL
     */
    public static String getDatabaseUrl() {
        return properties.getProperty("db.url", "jdbc:h2:tcp://localhost:9092/./data/employeedb");
    }

    /**
     * Get database username
     */
    public static String getDatabaseUsername() {
        return properties.getProperty("db.username", "sa");
    }

    /**
     * Get database password
     */
    public static String getDatabasePassword() {
        return properties.getProperty("db.password", "");
    }

    /**
     * Get thread pool size
     */
    public static int getThreadPoolSize() {
        return Integer.parseInt(properties.getProperty("server.thread.pool.size", "200"));
    }

    /**
     * Get number of employees to generate for performance testing
     */
    public static int getEmployeeGenerationCount() {
        return Integer.parseInt(properties.getProperty("data.generator.employee.count", "500000"));
    }

    /**
     * Get property value by key
     */
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Get connection pool size (Module 6: HikariCP)
     */
    public static int getPoolSize() {
        return Integer.parseInt(properties.getProperty("db.pool.size", "20"));
    }

    /**
     * Get connection timeout in milliseconds (Module 6: HikariCP)
     */
    public static long getPoolConnectionTimeout() {
        return Long.parseLong(properties.getProperty("db.pool.connection.timeout", "30000"));
    }

    /**
     * Get idle timeout in milliseconds (Module 6: HikariCP)
     */
    public static long getPoolIdleTimeout() {
        return Long.parseLong(properties.getProperty("db.pool.idle.timeout", "600000"));
    }

    /**
     * Get max lifetime in milliseconds (Module 6: HikariCP)
     */
    public static long getPoolMaxLifetime() {
        return Long.parseLong(properties.getProperty("db.pool.max.lifetime", "1800000"));
    }

    // Module 7: Cache Configuration

    /**
     * Get employee cache TTL in minutes
     */
    public static int getCacheEmployeeTtlMinutes() {
        return Integer.parseInt(properties.getProperty("cache.employee.ttl.minutes", "5"));
    }

    /**
     * Get employee cache max size
     */
    public static int getCacheEmployeeMaxSize() {
        return Integer.parseInt(properties.getProperty("cache.employee.max.size", "50000"));
    }

    /**
     * Get department cache TTL in minutes
     */
    public static int getCacheDepartmentTtlMinutes() {
        return Integer.parseInt(properties.getProperty("cache.department.ttl.minutes", "30"));
    }

    /**
     * Get department cache max size
     */
    public static int getCacheDepartmentMaxSize() {
        return Integer.parseInt(properties.getProperty("cache.department.max.size", "10"));
    }

    /**
     * Get department list cache TTL in minutes
     */
    public static int getCacheDepartmentListTtlMinutes() {
        return Integer.parseInt(properties.getProperty("cache.department.list.ttl.minutes", "30"));
    }

    /**
     * Get search cache TTL in minutes
     */
    public static int getCacheSearchTtlMinutes() {
        return Integer.parseInt(properties.getProperty("cache.search.ttl.minutes", "2"));
    }

    /**
     * Get search cache max size
     */
    public static int getCacheSearchMaxSize() {
        return Integer.parseInt(properties.getProperty("cache.search.max.size", "5000"));
    }
}
