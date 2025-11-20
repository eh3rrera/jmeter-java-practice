package com.example.employeeapi.dao;

import com.example.employeeapi.database.DatabaseManager;
import com.example.employeeapi.models.Department;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DepartmentDAO handles database operations for Department entities.
 * Uses naive JDBC with no connection pooling - each method creates a new connection.
 */
public class DepartmentDAO {

    /**
     * Get all departments
     */
    public List<Department> findAll() throws SQLException {
        String sql = "SELECT id, name, location, manager_id FROM departments";
        List<Department> departments = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Department dept = new Department();
                dept.setId(rs.getInt("id"));
                dept.setName(rs.getString("name"));
                dept.setLocation(rs.getString("location"));
                dept.setManagerId(rs.getObject("manager_id", Integer.class));
                departments.add(dept);
            }
        }

        return departments;
    }

    /**
     * Find department by ID
     */
    public Department findById(int id) throws SQLException {
        String sql = "SELECT id, name, location, manager_id FROM departments WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Department dept = new Department();
                dept.setId(rs.getInt("id"));
                dept.setName(rs.getString("name"));
                dept.setLocation(rs.getString("location"));
                dept.setManagerId(rs.getObject("manager_id", Integer.class));
                return dept;
            }
            return null;
        }
    }
}
