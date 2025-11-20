package com.example.employeeapi.handlers;

import com.example.employeeapi.dao.CachedDepartmentDAO;
import com.example.employeeapi.dao.CachedEmployeeDAO;
import com.example.employeeapi.dao.DepartmentDAO;
import com.example.employeeapi.dao.EmployeeDAO;
import com.example.employeeapi.models.Department;
import com.example.employeeapi.models.Employee;
import com.example.employeeapi.util.JsonUtil;
import com.example.employeeapi.util.JwtUtil;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.employeeapi.database.DatabaseManager.getConnection;

/**
 * ReportHandler handles analytics and reporting endpoints with simulated external service calls.
 * All endpoints require JWT authentication.
 * Module 7: Uses cached DAOs for employee/department lookups while preserving simulated delays.
 * These endpoints include Thread.sleep() calls to simulate blocking I/O operations for performance testing.
 * - GET /api/reports/employee-profile/{id} - Comprehensive employee profile with external service simulation
 * - GET /api/reports/department-analytics/{id} - Department analytics with external service simulation
 * - GET /api/dashboard/company-overview - Company-wide statistics with multiple external service simulations
 */
public class ReportHandler implements HttpHandler {
    private final EmployeeDAO employeeDAO = new CachedEmployeeDAO();
    private final DepartmentDAO departmentDAO = new CachedDepartmentDAO();

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
            // GET /api/reports/employee-profile/{id}
            if (path.matches("/api/reports/employee-profile/\\d+")) {
                handleEmployeeProfile(exchange, path);
                return;
            }

            // GET /api/reports/department-analytics/{id}
            if (path.matches("/api/reports/department-analytics/\\d+")) {
                handleDepartmentAnalytics(exchange, path);
                return;
            }

            // GET /api/dashboard/company-overview
            if (path.equals("/api/dashboard/company-overview")) {
                handleCompanyOverview(exchange);
                return;
            }

            sendResponse(exchange, 404, JsonUtil.errorResponse("Not found"));

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, JsonUtil.errorResponse("Error generating report: " + e.getMessage()));
        }
    }

    /**
     * Employee profile with simulated external service calls
     */
    private void handleEmployeeProfile(HttpExchange exchange, String path) throws Exception {
        String[] parts = path.split("/");
        int employeeId = Integer.parseInt(parts[4]);

        // 1. Fetch employee data
        Employee employee = employeeDAO.findById(employeeId);
        if (employee == null) {
            sendResponse(exchange, 404, JsonUtil.errorResponse("Employee not found"));
            return;
        }

        // 2. Fetch department data
        Department department = null;
        if (employee.getDepartmentId() != null) {
            department = departmentDAO.findById(employee.getDepartmentId());
        }

        // 3. SIMULATE external "background verification service" (200ms)
        Thread.sleep(200);

        // 4. SIMULATE external "payroll system query" (150ms)
        Thread.sleep(150);

        // 5. Calculate peer salary comparison
        BigDecimal avgDepartmentSalary = null;
        if (employee.getDepartmentId() != null) {
            avgDepartmentSalary = calculateAverageSalary(employee.getDepartmentId());
        }

        // Build response
        JsonObject response = new JsonObject();
        response.add("employee", JsonUtil.fromJson(JsonUtil.toJson(employee), JsonObject.class));
        if (department != null) {
            response.add("department", JsonUtil.fromJson(JsonUtil.toJson(department), JsonObject.class));
        }
        response.addProperty("backgroundVerificationStatus", "Verified");
        response.addProperty("payrollStatus", "Active");
        if (avgDepartmentSalary != null) {
            response.addProperty("departmentAverageSalary", avgDepartmentSalary);
        }

        sendResponse(exchange, 200, JsonUtil.toJson(response));
    }

    /**
     * Department analytics with simulated external service calls
     */
    private void handleDepartmentAnalytics(HttpExchange exchange, String path) throws Exception {
        String[] parts = path.split("/");
        int departmentId = Integer.parseInt(parts[4]);

        // Fetch department
        Department department = departmentDAO.findById(departmentId);
        if (department == null) {
            sendResponse(exchange, 404, JsonUtil.errorResponse("Department not found"));
            return;
        }

        // 1. Aggregate salary statistics
        Map<String, Object> salaryStats = calculateSalaryStatistics(departmentId);

        // 2. Count employees by hire year
        Map<Integer, Integer> employeesByYear = countEmployeesByHireYear(departmentId);

        // 3. SIMULATE "HR system integration" (300ms)
        Thread.sleep(300);

        // 4. SIMULATE "benefits provider API" (200ms)
        Thread.sleep(200);

        // Build response
        JsonObject response = new JsonObject();
        response.add("department", JsonUtil.fromJson(JsonUtil.toJson(department), JsonObject.class));
        response.add("salaryStatistics", JsonUtil.fromJson(JsonUtil.toJson(salaryStats), JsonObject.class));
        response.add("employeesByHireYear", JsonUtil.fromJson(JsonUtil.toJson(employeesByYear), JsonObject.class));
        response.addProperty("hrSystemStatus", "Connected");
        response.addProperty("benefitsProviderStatus", "Active");

        sendResponse(exchange, 200, JsonUtil.toJson(response));
    }

    /**
     * Company overview with multiple simulated external service calls
     */
    private void handleCompanyOverview(HttpExchange exchange) throws Exception {
        // 1. Total employee count
        int totalEmployees = employeeDAO.getTotalCount();

        // 2. Department count
        int departmentCount = departmentDAO.findAll().size();

        // 3. Average company salary
        BigDecimal avgSalary = calculateCompanyAverageSalary();

        // 4. SIMULATE "financial system API" (250ms)
        Thread.sleep(250);

        // 5. SIMULATE "compliance check service" (200ms)
        Thread.sleep(200);

        // 6. SIMULATE "attendance system" (150ms)
        Thread.sleep(150);

        // Build response
        JsonObject response = new JsonObject();
        response.addProperty("totalEmployees", totalEmployees);
        response.addProperty("totalDepartments", departmentCount);
        response.addProperty("averageSalary", avgSalary);
        response.addProperty("financialSystemStatus", "Operational");
        response.addProperty("complianceStatus", "Compliant");
        response.addProperty("attendanceSystemStatus", "Online");

        sendResponse(exchange, 200, JsonUtil.toJson(response));
    }

    // Helper methods for database calculations

    private BigDecimal calculateAverageSalary(int departmentId) throws Exception {
        String sql = "SELECT AVG(salary) as avg_salary FROM employees WHERE department_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, departmentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                BigDecimal avg = rs.getBigDecimal("avg_salary");
                return avg != null ? avg.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }

    private Map<String, Object> calculateSalaryStatistics(int departmentId) throws Exception {
        String sql = "SELECT MIN(salary) as min_sal, MAX(salary) as max_sal, " +
                    "AVG(salary) as avg_sal, COUNT(*) as emp_count " +
                    "FROM employees WHERE department_id = ?";

        Map<String, Object> stats = new HashMap<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, departmentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                stats.put("minSalary", rs.getBigDecimal("min_sal"));
                stats.put("maxSalary", rs.getBigDecimal("max_sal"));
                BigDecimal avg = rs.getBigDecimal("avg_sal");
                stats.put("averageSalary", avg != null ? avg.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
                stats.put("employeeCount", rs.getInt("emp_count"));
            }
        }
        return stats;
    }

    private Map<Integer, Integer> countEmployeesByHireYear(int departmentId) throws Exception {
        String sql = "SELECT YEAR(hire_date) as hire_year, COUNT(*) as count " +
                    "FROM employees WHERE department_id = ? " +
                    "GROUP BY YEAR(hire_date) ORDER BY hire_year";

        Map<Integer, Integer> employeesByYear = new HashMap<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, departmentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                employeesByYear.put(rs.getInt("hire_year"), rs.getInt("count"));
            }
        }
        return employeesByYear;
    }

    private BigDecimal calculateCompanyAverageSalary() throws Exception {
        String sql = "SELECT AVG(salary) as avg_salary FROM employees";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                BigDecimal avg = rs.getBigDecimal("avg_salary");
                return avg != null ? avg.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
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
