package com.example.employeeapi.dao;

import com.example.employeeapi.database.DatabaseManager;
import com.example.employeeapi.models.Employee;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * EmployeeDAO handles database operations for Employee entities.
 * Uses naive JDBC with no connection pooling - each method creates a new connection.
 */
public class EmployeeDAO {

    /**
     * Find employee by ID
     */
    public Employee findById(int id) throws SQLException {
        String sql = "SELECT id, name, email, department_id, salary, hire_date, phone, address " +
                    "FROM employees WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToEmployee(rs);
            }
            return null;
        }
    }

    /**
     * Search employees by name (case-insensitive, prefix match)
     * Uses indexed computed column (name_lower) for optimal performance.
     * Pattern is "starts with" rather than "contains" to enable index range scans.
     */
    public List<Employee> searchByName(String name) throws SQLException {
        String sql = "SELECT id, name, email, department_id, salary, hire_date, phone, address " +
                    "FROM employees WHERE name_lower LIKE ?";
        List<Employee> employees = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Use prefix match (name%) instead of contains (%name%) for index efficiency
            // Convert search term to lowercase to match the computed column
            stmt.setString(1, name.toLowerCase() + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                employees.add(mapResultSetToEmployee(rs));
            }
        }

        return employees;
    }

    /**
     * Find all employees in a department
     */
    public List<Employee> findByDepartmentId(int departmentId) throws SQLException {
        String sql = "SELECT id, name, email, department_id, salary, hire_date, phone, address " +
                    "FROM employees WHERE department_id = ?";
        List<Employee> employees = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, departmentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                employees.add(mapResultSetToEmployee(rs));
            }
        }

        return employees;
    }

    /**
     * Count employees by department
     */
    public int countByDepartmentId(int departmentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM employees WHERE department_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, departmentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    /**
     * Get total employee count
     */
    public int getTotalCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM employees";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    /**
     * Helper method to map ResultSet to Employee object
     */
    private Employee mapResultSetToEmployee(ResultSet rs) throws SQLException {
        Employee employee = new Employee();
        employee.setId(rs.getInt("id"));
        employee.setName(rs.getString("name"));
        employee.setEmail(rs.getString("email"));
        employee.setDepartmentId(rs.getObject("department_id", Integer.class));
        employee.setSalary(rs.getBigDecimal("salary"));

        Date hireDate = rs.getDate("hire_date");
        if (hireDate != null) {
            employee.setHireDate(hireDate.toLocalDate());
        }

        employee.setPhone(rs.getString("phone"));
        employee.setAddress(rs.getString("address"));
        return employee;
    }
}
