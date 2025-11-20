package com.example.employeeapi.database;

import com.example.employeeapi.util.ConfigUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.h2.tools.Server;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * DatabaseManager handles H2 database server startup, schema initialization,
 * and provides database connections via HikariCP connection pool.
 *
 * MODULE 6 IMPLEMENTATION: Uses HikariCP for connection pooling.
 * This eliminates the overhead of creating new connections for each request,
 * dramatically improving performance for database-heavy operations.
 */
public class DatabaseManager {
    private static Server h2Server;
    private static HikariDataSource dataSource;

    /**
     * Start H2 database server in TCP mode
     */
    public static void startH2Server() throws SQLException {
        try {
            // -tcpAllowOthers: Allow remote connections (for JMeter testing)
            // -ifNotExists: Allow database creation if it doesn't exist
            h2Server = Server.createTcpServer(
                "-tcp",
                "-tcpAllowOthers",
                "-tcpPort", "9092",
                "-ifNotExists"
            ).start();
            System.out.println("H2 Database server started on port 9092");
        } catch (SQLException e) {
            System.err.println("Failed to start H2 server: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Stop H2 database server
     */
    public static void stopH2Server() {
        if (h2Server != null) {
            h2Server.stop();
            System.out.println("H2 Database server stopped");
        }
    }

    /**
     * Initialize HikariCP connection pool (Module 6)
     * Must be called after H2 server starts and before any database operations.
     */
    public static void initializeConnectionPool() {
        System.out.println("Initializing HikariCP connection pool...");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(ConfigUtil.getDatabaseUrl());
        config.setUsername(ConfigUtil.getDatabaseUsername());
        config.setPassword(ConfigUtil.getDatabasePassword());

        // Pool configuration
        config.setMaximumPoolSize(ConfigUtil.getPoolSize());
        config.setConnectionTimeout(ConfigUtil.getPoolConnectionTimeout());
        config.setIdleTimeout(ConfigUtil.getPoolIdleTimeout());
        config.setMaxLifetime(ConfigUtil.getPoolMaxLifetime());

        // H2-specific settings
        config.setConnectionTestQuery("SELECT 1");
        config.setPoolName("EmployeeAPI-HikariPool");

        // Performance tuning
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);

        System.out.println("HikariCP connection pool initialized:");
        System.out.println("  Pool name: " + dataSource.getPoolName());
        System.out.println("  Maximum pool size: " + dataSource.getMaximumPoolSize());
        System.out.println("  Connection timeout: " + dataSource.getConnectionTimeout() + "ms");
    }

    /**
     * Close HikariCP connection pool
     * Should be called during application shutdown for graceful cleanup.
     */
    public static void closeConnectionPool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("HikariCP connection pool closed");
        }
    }

    /**
     * Get connection from HikariCP pool (Module 6)
     * Reuses existing connections from the pool instead of creating new ones.
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Connection pool not initialized. Call initializeConnectionPool() first.");
        }
        return dataSource.getConnection();
    }

    /**
     * Get connection directly via DriverManager (used for initialization tasks)
     * This bypasses the connection pool and is only used during setup.
     */
    private static Connection getDirectConnection() throws SQLException {
        return DriverManager.getConnection(
            ConfigUtil.getDatabaseUrl(),
            ConfigUtil.getDatabaseUsername(),
            ConfigUtil.getDatabasePassword()
        );
    }

    /**
     * Initialize database schema (create tables if they don't exist)
     */
    public static void initializeSchema() throws SQLException {
        System.out.println("Initializing database schema from schema.sql...");
        executeSqlFile("schema.sql");
        System.out.println("Database schema initialized");
    }

    /**
     * Insert sample data if tables are empty
     * Uses direct connection for initialization (bypasses pool)
     */
    public static void insertSampleData() throws SQLException {
        try (Connection conn = getDirectConnection();
             Statement stmt = conn.createStatement()) {

            // Check if data already exists
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Sample data already exists, skipping insertion");
                return;
            }

            System.out.println("Inserting sample data...");

            // Execute data.sql for users and departments only
            executeSqlFile("data.sql");

            System.out.println("Sample users and departments inserted.");

            // Generate bulk employees for performance testing
            int employeeCount = ConfigUtil.getEmployeeGenerationCount();
            System.out.println("Generating " + employeeCount + " employees for performance testing...");
            generateBulkEmployees(employeeCount);

            System.out.println("Sample data insertion complete.");
        }
    }

    /**
     * Generate and insert large number of employees using batch processing.
     * Uses direct connection for bulk initialization (bypasses pool).
     *
     * @param totalEmployees Number of employees to generate
     */
    private static void generateBulkEmployees(int totalEmployees) throws SQLException {
        final int BATCH_SIZE = 1000;
        final int PROGRESS_INTERVAL = 50000;
        final int NUM_DEPARTMENTS = 5; // We have 5 departments (1-5)

        String insertSQL = "INSERT INTO employees (name, email, department_id, salary, hire_date, phone, address) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?)";

        long startTime = System.currentTimeMillis();
        int employeesInserted = 0;

        try (Connection conn = getDirectConnection()) {
            conn.setAutoCommit(false); // Use transaction for better performance

            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                for (int i = 1; i <= totalEmployees; i++) {
                    // Distribute employees across departments evenly
                    int departmentId = ((i - 1) % NUM_DEPARTMENTS) + 1;

                    // Generate employee data
                    DataGenerator.Employee emp = DataGenerator.generateRandomEmployee(i, departmentId);

                    // Set parameters
                    pstmt.setString(1, emp.name);
                    pstmt.setString(2, emp.email);
                    pstmt.setInt(3, emp.departmentId);
                    pstmt.setBigDecimal(4, emp.salary);
                    pstmt.setDate(5, java.sql.Date.valueOf(emp.hireDate));
                    pstmt.setString(6, emp.phone);
                    pstmt.setString(7, emp.address);

                    pstmt.addBatch();
                    employeesInserted++;

                    // Execute batch every BATCH_SIZE records
                    if (i % BATCH_SIZE == 0) {
                        pstmt.executeBatch();
                        conn.commit();

                        // Progress reporting
                        if (i % PROGRESS_INTERVAL == 0) {
                            long elapsed = System.currentTimeMillis() - startTime;
                            double rate = (employeesInserted / (elapsed / 1000.0));
                            System.out.printf("  Progress: %,d / %,d employees (%.0f records/sec)%n",
                                            i, totalEmployees, rate);
                        }
                    }
                }

                // Execute remaining batch
                pstmt.executeBatch();
                conn.commit();

                long elapsed = System.currentTimeMillis() - startTime;
                double avgRate = (employeesInserted / (elapsed / 1000.0));
                System.out.printf("Completed: %,d employees inserted in %.2f seconds (%.0f records/sec)%n",
                                employeesInserted, elapsed / 1000.0, avgRate);

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Execute SQL statements from a file in the classpath
     * Uses direct connection for schema initialization (bypasses pool)
     */
    private static void executeSqlFile(String filename) throws SQLException {
        try (Connection conn = getDirectConnection();
             Statement stmt = conn.createStatement()) {

            InputStream inputStream = DatabaseManager.class.getClassLoader().getResourceAsStream(filename);
            if (inputStream == null) {
                throw new SQLException("SQL file not found: " + filename);
            }

            String sql = new BufferedReader(new InputStreamReader(inputStream))
                    .lines()
                    .filter(line -> !line.trim().startsWith("--")) // Remove SQL comments
                    .filter(line -> !line.trim().isEmpty())        // Remove empty lines
                    .collect(Collectors.joining("\n"));

            // Split by semicolon and execute each statement
            String[] statements = sql.split(";");
            for (String statement : statements) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }

        } catch (Exception e) {
            throw new SQLException("Error executing SQL file: " + filename, e);
        }
    }
}
