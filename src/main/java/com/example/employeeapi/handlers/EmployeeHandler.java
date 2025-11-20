package com.example.employeeapi.handlers;

import com.example.employeeapi.dao.CachedEmployeeDAO;
import com.example.employeeapi.dao.EmployeeDAO;
import com.example.employeeapi.models.Employee;
import com.example.employeeapi.util.JsonUtil;
import com.example.employeeapi.util.JwtUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * EmployeeHandler handles employee-related endpoints.
 * All endpoints require JWT authentication.
 * Module 7: Uses CachedEmployeeDAO for improved performance.
 * - GET /api/employees/{id} - Get employee by ID (cached)
 * - GET /api/employees/search?name={name} - Search employees by name (cached)
 */
public class EmployeeHandler implements HttpHandler {
    private final EmployeeDAO employeeDAO = new CachedEmployeeDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        long startTime = System.currentTimeMillis();
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            // Validate JWT token
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendResponse(exchange, 401, JsonUtil.errorResponse("Missing or invalid authorization header"));
                return;
            }

            String token = authHeader.substring(7);
            String username = JwtUtil.validateToken(token);
            if (username == null) {
                sendResponse(exchange, 401, JsonUtil.errorResponse("Invalid or expired token"));
                return;
            }

            // Route request
            if ("GET".equals(method)) {
                handleGet(exchange);
            } else {
                sendResponse(exchange, 405, JsonUtil.errorResponse("Method not allowed"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, JsonUtil.errorResponse("Internal server error: " + e.getMessage()));
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            System.out.printf("[%s] %s - Status: %d - Duration: %dms%n",
                    method, path, exchange.getResponseCode(), duration);
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();

        try {
            // Check if it's a search query
            if (query != null && query.startsWith("name=")) {
                String name = query.substring(5);
                List<Employee> employees = employeeDAO.searchByName(name);
                sendResponse(exchange, 200, JsonUtil.toJson(employees));
                return;
            }

            // Extract employee ID from path: /api/employees/{id}
            String[] parts = path.split("/");
            if (parts.length >= 4) {
                try {
                    int id = Integer.parseInt(parts[3]);
                    Employee employee = employeeDAO.findById(id);

                    if (employee == null) {
                        sendResponse(exchange, 404, JsonUtil.errorResponse("Employee not found"));
                    } else {
                        sendResponse(exchange, 200, JsonUtil.toJson(employee));
                    }
                } catch (NumberFormatException e) {
                    sendResponse(exchange, 400, JsonUtil.errorResponse("Invalid employee ID"));
                }
            } else {
                sendResponse(exchange, 400, JsonUtil.errorResponse("Invalid request"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, JsonUtil.errorResponse("Database error: " + e.getMessage()));
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
