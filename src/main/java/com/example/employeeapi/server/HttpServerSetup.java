package com.example.employeeapi.server;

import com.example.employeeapi.handlers.DepartmentHandler;
import com.example.employeeapi.handlers.EmployeeHandler;
import com.example.employeeapi.handlers.LoginHandler;
import com.example.employeeapi.handlers.ReportHandler;
import com.example.employeeapi.util.ConfigUtil;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * HttpServerSetup configures and creates the HTTP server with route registration.
 * MODULE 5 IMPLEMENTATION: Uses virtual threads for unbounded concurrency.
 * Virtual threads allow handling massive concurrent requests without thread pool limits.
 */
public class HttpServerSetup {

    /**
     * Create and configure HTTP server with VIRTUAL THREADS
     * Module 5: Switched from platform threads to virtual threads for improved I/O performance
     */
    public static HttpServer createServer() throws IOException {
        int port = ConfigUtil.getServerPort();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // MODULE 5: Use virtual threads for unbounded concurrency
        // Virtual threads are lightweight and perfect for I/O-bound operations
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        server.setExecutor(executor);

        System.out.println("Thread pool configured: Virtual threads (unbounded concurrency)");

        // Register handlers
        registerRoutes(server);

        return server;
    }

    /**
     * Register all API routes
     */
    private static void registerRoutes(HttpServer server) {
        // Authentication
        server.createContext("/api/login", new LoginHandler());

        // Employee endpoints (require JWT)
        server.createContext("/api/employees", new EmployeeHandler());

        // Department endpoints
        server.createContext("/api/departments", new DepartmentHandler());

        // Report endpoints (with simulated I/O delays)
        server.createContext("/api/reports", new ReportHandler());
        server.createContext("/api/dashboard", new ReportHandler());

        System.out.println("Routes registered:");
        System.out.println("  POST /api/login");
        System.out.println("  GET  /api/employees/{id}");
        System.out.println("  GET  /api/employees/search?name={name}");
        System.out.println("  GET  /api/departments");
        System.out.println("  GET  /api/departments/{id}/employees");
        System.out.println("  GET  /api/reports/employee-profile/{id}");
        System.out.println("  GET  /api/reports/department-analytics/{id}");
        System.out.println("  GET  /api/dashboard/company-overview");
    }

    public static int getPort() {
        return ConfigUtil.getServerPort();
    }
}
