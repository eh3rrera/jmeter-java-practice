package com.example.employeeapi;

import com.example.employeeapi.cache.CacheManager;
import com.example.employeeapi.database.DatabaseManager;
import com.example.employeeapi.server.HttpServerSetup;
import com.sun.net.httpserver.HttpServer;

/**
 * Main application entry point.
 * Starts H2 database server, initializes schema, connection pool, cache, and HTTP server.
 * Module 7: Added Caffeine cache initialization for improved performance.
 */
public class Main {
    private static HttpServer httpServer;

    public static void main(String[] args) {
        try {
            System.out.println("=== Employee Management API ===");
            System.out.println("Starting application...\n");

            // 1. Start H2 database server
            System.out.println("1. Starting H2 database server...");
            DatabaseManager.startH2Server();

            // 2. Initialize database schema
            System.out.println("2. Initializing database schema...");
            DatabaseManager.initializeSchema();

            // 3. Insert sample data if needed
            System.out.println("3. Checking and inserting sample data...");
            DatabaseManager.insertSampleData();

            // 4. Initialize HikariCP connection pool
            System.out.println("\n4. Initializing connection pool...");
            DatabaseManager.initializeConnectionPool();

            // 5. Initialize Caffeine cache
            System.out.println("\n5. Initializing cache layer...");
            CacheManager.initialize();

            // 6. Create and start HTTP server
            System.out.println("\n6. Starting HTTP server...");
            httpServer = HttpServerSetup.createServer();
            httpServer.start();

            System.out.println("\n=================================");
            System.out.println("Server started on http://localhost:" + HttpServerSetup.getPort());
            System.out.println("=================================");
            System.out.println("\nDatabase Connection Info:");
            System.out.println("  URL: jdbc:h2:tcp://localhost:9092/./data/employeedb");
            System.out.println("  Username: sa");
            System.out.println("  Password: (empty)");
            System.out.println("\nTest Users:");
            System.out.println("  99 users available: user01 to user99");
            System.out.println("  Password for each user matches the username");
            System.out.println("  Examples: user01/user01, user02/user02, user03/user03");
            System.out.println("\nPress Ctrl+C to stop the server");

            // Add shutdown hook for graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n\nShutting down server...");
                if (httpServer != null) {
                    httpServer.stop(0);
                    System.out.println("HTTP server stopped");
                }
                CacheManager.close(); // Module 7: Print cache stats and cleanup
                DatabaseManager.closeConnectionPool();
                DatabaseManager.stopH2Server();
                System.out.println("Shutdown complete");
            }));

        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
