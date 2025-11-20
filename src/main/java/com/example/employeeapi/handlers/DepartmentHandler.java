package com.example.employeeapi.handlers;

import com.example.employeeapi.dao.CachedDepartmentDAO;
import com.example.employeeapi.dao.CachedEmployeeDAO;
import com.example.employeeapi.dao.DepartmentDAO;
import com.example.employeeapi.dao.EmployeeDAO;
import com.example.employeeapi.models.Department;
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
 * DepartmentHandler handles department-related endpoints.
 * All endpoints require JWT authentication.
 * Module 7: Uses cached DAOs for improved performance.
 * - GET /api/departments - Get all departments (cached)
 * - GET /api/departments/{id}/employees - Get all employees in a department
 */
public class DepartmentHandler implements HttpHandler {
    private final DepartmentDAO departmentDAO = new CachedDepartmentDAO();
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

        try {
            // Check if it's /api/departments/{id}/employees
            if (path.matches("/api/departments/\\d+/employees")) {
                String[] parts = path.split("/");
                int departmentId = Integer.parseInt(parts[3]);

                List<Employee> employees = employeeDAO.findByDepartmentId(departmentId);
                sendResponse(exchange, 200, JsonUtil.toJson(employees));
                return;
            }

            // Otherwise, it's GET /api/departments
            if (path.equals("/api/departments")) {
                List<Department> departments = departmentDAO.findAll();
                sendResponse(exchange, 200, JsonUtil.toJson(departments));
                return;
            }

            sendResponse(exchange, 404, JsonUtil.errorResponse("Not found"));

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
