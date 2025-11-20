package com.example.employeeapi.dao;

import com.example.employeeapi.cache.CacheManager;
import com.example.employeeapi.models.Employee;

import java.sql.SQLException;
import java.util.List;

/**
 * CachedEmployeeDAO wraps EmployeeDAO with Caffeine caching layer.
 * Module 7 implementation: Reduces database load for read-heavy operations.
 *
 * Caching Strategy:
 * - findById: Cache individual employees (50k max, 5 min TTL)
 * - searchByName: Cache search results (5k max, 2 min TTL)
 * - Other methods: Pass through to underlying DAO (no caching)
 */
public class CachedEmployeeDAO extends EmployeeDAO {

    /**
     * Find employee by ID with caching
     * Cache-aside pattern: check cache first, then database if miss
     */
    @Override
    public Employee findById(int id) throws SQLException {
        // Try cache first
        Employee cachedEmployee = CacheManager.getEmployee(id);
        if (cachedEmployee != null) {
            return cachedEmployee;
        }

        // Cache miss - fetch from database
        Employee employee = super.findById(id);

        // Store in cache if found
        if (employee != null) {
            CacheManager.putEmployee(id, employee);
        }

        return employee;
    }

    /**
     * Search employees by name with caching
     * Cache key: lowercase search term for consistency
     */
    @Override
    public List<Employee> searchByName(String name) throws SQLException {
        // Try cache first
        List<Employee> cachedResults = CacheManager.getSearchResults(name);
        if (cachedResults != null) {
            return cachedResults;
        }

        // Cache miss - fetch from database
        List<Employee> results = super.searchByName(name);

        // Store in cache
        CacheManager.putSearchResults(name, results);

        return results;
    }

    /**
     * Find employees by department ID
     * No caching: result set changes frequently and is department-specific
     */
    @Override
    public List<Employee> findByDepartmentId(int departmentId) throws SQLException {
        return super.findByDepartmentId(departmentId);
    }

    /**
     * Count employees by department ID
     * No caching: used for analytics, needs fresh data
     */
    @Override
    public int countByDepartmentId(int departmentId) throws SQLException {
        return super.countByDepartmentId(departmentId);
    }

    /**
     * Get total employee count
     * No caching: needs accurate count
     */
    @Override
    public int getTotalCount() throws SQLException {
        return super.getTotalCount();
    }
}
