package com.example.employeeapi.dao;

import com.example.employeeapi.cache.CacheManager;
import com.example.employeeapi.models.Department;

import java.sql.SQLException;
import java.util.List;

/**
 * CachedDepartmentDAO wraps DepartmentDAO with Caffeine caching layer.
 * Module 7 implementation: Reduces database load for read-heavy operations.
 *
 * Caching Strategy:
 * - findById: Cache individual departments (10 max, 30 min TTL) - all 5 departments fit
 * - findAll: Cache complete department list (1 entry, 30 min TTL) - static data
 *
 * Expected cache hit ratio: ~99% (departments are static and frequently accessed)
 */
public class CachedDepartmentDAO extends DepartmentDAO {

    /**
     * Find all departments with caching
     * Cache-aside pattern: check cache first, then database if miss
     * Single cache entry for the complete department list
     */
    @Override
    public List<Department> findAll() throws SQLException {
        // Try cache first
        List<Department> cachedList = CacheManager.getDepartmentList();
        if (cachedList != null) {
            return cachedList;
        }

        // Cache miss - fetch from database
        List<Department> departments = super.findAll();

        // Store in cache
        CacheManager.putDepartmentList(departments);

        return departments;
    }

    /**
     * Find department by ID with caching
     * Cache-aside pattern: check cache first, then database if miss
     */
    @Override
    public Department findById(int id) throws SQLException {
        // Try cache first
        Department cachedDepartment = CacheManager.getDepartment(id);
        if (cachedDepartment != null) {
            return cachedDepartment;
        }

        // Cache miss - fetch from database
        Department department = super.findById(id);

        // Store in cache if found
        if (department != null) {
            CacheManager.putDepartment(id, department);
        }

        return department;
    }
}
