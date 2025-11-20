package com.example.employeeapi.cache;

import com.example.employeeapi.models.Department;
import com.example.employeeapi.models.Employee;
import com.example.employeeapi.util.ConfigUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * CacheManager handles all caching operations using Caffeine.
 * Module 7 implementation: Reduces database load for read-heavy operations.
 *
 * Cache Strategy:
 * - Employee by ID: 50k entries, 5 min TTL (for 500k employee dataset)
 * - Department by ID: 10 entries, 30 min TTL (only 5 departments exist)
 * - Department list: 1 entry, 30 min TTL (findAll result)
 * - Search results: 5k entries, 2 min TTL (repeated searches)
 */
public class CacheManager {

    private static Cache<Integer, Employee> employeeCache;
    private static Cache<Integer, Department> departmentCache;
    private static Cache<String, List<Department>> departmentListCache;
    private static Cache<String, List<Employee>> searchCache;

    /**
     * Initialize all caches with configuration from application.properties
     */
    public static void initialize() {
        System.out.println("Initializing Caffeine cache layer ...");

        // Employee cache: for findById operations
        employeeCache = Caffeine.newBuilder()
                .maximumSize(ConfigUtil.getCacheEmployeeMaxSize())
                .expireAfterWrite(ConfigUtil.getCacheEmployeeTtlMinutes(), TimeUnit.MINUTES)
                .recordStats()
                .build();

        // Department cache: for findById operations
        departmentCache = Caffeine.newBuilder()
                .maximumSize(ConfigUtil.getCacheDepartmentMaxSize())
                .expireAfterWrite(ConfigUtil.getCacheDepartmentTtlMinutes(), TimeUnit.MINUTES)
                .recordStats()
                .build();

        // Department list cache: for findAll operations (single entry)
        departmentListCache = Caffeine.newBuilder()
                .maximumSize(1)
                .expireAfterWrite(ConfigUtil.getCacheDepartmentListTtlMinutes(), TimeUnit.MINUTES)
                .recordStats()
                .build();

        // Search cache: for searchByName operations
        searchCache = Caffeine.newBuilder()
                .maximumSize(ConfigUtil.getCacheSearchMaxSize())
                .expireAfterWrite(ConfigUtil.getCacheSearchTtlMinutes(), TimeUnit.MINUTES)
                .recordStats()
                .build();

        System.out.println("Cache configuration:");
        System.out.println("  Employee cache: max=" + ConfigUtil.getCacheEmployeeMaxSize() +
                         " entries, TTL=" + ConfigUtil.getCacheEmployeeTtlMinutes() + " min");
        System.out.println("  Department cache: max=" + ConfigUtil.getCacheDepartmentMaxSize() +
                         " entries, TTL=" + ConfigUtil.getCacheDepartmentTtlMinutes() + " min");
        System.out.println("  Department list cache: max=1 entry, TTL=" +
                         ConfigUtil.getCacheDepartmentListTtlMinutes() + " min");
        System.out.println("  Search cache: max=" + ConfigUtil.getCacheSearchMaxSize() +
                         " entries, TTL=" + ConfigUtil.getCacheSearchTtlMinutes() + " min");
        System.out.println("Caffeine cache layer initialized successfully");
    }

    // Employee cache operations

    public static Employee getEmployee(int id) {
        return employeeCache.getIfPresent(id);
    }

    public static void putEmployee(int id, Employee employee) {
        employeeCache.put(id, employee);
    }

    public static void invalidateEmployee(int id) {
        employeeCache.invalidate(id);
    }

    // Department cache operations

    public static Department getDepartment(int id) {
        return departmentCache.getIfPresent(id);
    }

    public static void putDepartment(int id, Department department) {
        departmentCache.put(id, department);
    }

    public static void invalidateDepartment(int id) {
        departmentCache.invalidate(id);
    }

    // Department list cache operations

    public static List<Department> getDepartmentList() {
        return departmentListCache.getIfPresent("all");
    }

    public static void putDepartmentList(List<Department> departments) {
        departmentListCache.put("all", departments);
    }

    public static void invalidateDepartmentList() {
        departmentListCache.invalidate("all");
    }

    // Search cache operations

    public static List<Employee> getSearchResults(String searchTerm) {
        // Normalize search term to lowercase for consistent cache keys
        String cacheKey = searchTerm.toLowerCase();
        return searchCache.getIfPresent(cacheKey);
    }

    public static void putSearchResults(String searchTerm, List<Employee> results) {
        // Normalize search term to lowercase for consistent cache keys
        String cacheKey = searchTerm.toLowerCase();
        searchCache.put(cacheKey, results);
    }

    // Cache management operations

    /**
     * Clear all caches
     */
    public static void clearAll() {
        employeeCache.invalidateAll();
        departmentCache.invalidateAll();
        departmentListCache.invalidateAll();
        searchCache.invalidateAll();
        System.out.println("All caches cleared");
    }

    /**
     * Print cache statistics (hit/miss ratios, evictions, etc.)
     */
    public static void printStatistics() {
        System.out.println("\n========== Cache Statistics ==========");

        CacheStats employeeStats = employeeCache.stats();
        System.out.println("Employee Cache:");
        System.out.println("  Size: " + employeeCache.estimatedSize() + " / " +
                         ConfigUtil.getCacheEmployeeMaxSize());
        System.out.println("  Requests: " + employeeStats.requestCount());
        System.out.println("  Hits: " + employeeStats.hitCount() + " (" +
                         String.format("%.2f%%", employeeStats.hitRate() * 100) + ")");
        System.out.println("  Misses: " + employeeStats.missCount() + " (" +
                         String.format("%.2f%%", employeeStats.missRate() * 100) + ")");
        System.out.println("  Evictions: " + employeeStats.evictionCount());

        CacheStats deptStats = departmentCache.stats();
        System.out.println("\nDepartment Cache:");
        System.out.println("  Size: " + departmentCache.estimatedSize() + " / " +
                         ConfigUtil.getCacheDepartmentMaxSize());
        System.out.println("  Requests: " + deptStats.requestCount());
        System.out.println("  Hits: " + deptStats.hitCount() + " (" +
                         String.format("%.2f%%", deptStats.hitRate() * 100) + ")");
        System.out.println("  Misses: " + deptStats.missCount() + " (" +
                         String.format("%.2f%%", deptStats.missRate() * 100) + ")");
        System.out.println("  Evictions: " + deptStats.evictionCount());

        CacheStats deptListStats = departmentListCache.stats();
        System.out.println("\nDepartment List Cache:");
        System.out.println("  Size: " + departmentListCache.estimatedSize() + " / 1");
        System.out.println("  Requests: " + deptListStats.requestCount());
        System.out.println("  Hits: " + deptListStats.hitCount() + " (" +
                         String.format("%.2f%%", deptListStats.hitRate() * 100) + ")");
        System.out.println("  Misses: " + deptListStats.missCount() + " (" +
                         String.format("%.2f%%", deptListStats.missRate() * 100) + ")");

        CacheStats searchStats = searchCache.stats();
        System.out.println("\nSearch Results Cache:");
        System.out.println("  Size: " + searchCache.estimatedSize() + " / " +
                         ConfigUtil.getCacheSearchMaxSize());
        System.out.println("  Requests: " + searchStats.requestCount());
        System.out.println("  Hits: " + searchStats.hitCount() + " (" +
                         String.format("%.2f%%", searchStats.hitRate() * 100) + ")");
        System.out.println("  Misses: " + searchStats.missCount() + " (" +
                         String.format("%.2f%%", searchStats.missRate() * 100) + ")");
        System.out.println("  Evictions: " + searchStats.evictionCount());

        System.out.println("=================================================\n");
    }

    /**
     * Close and cleanup caches
     */
    public static void close() {
        System.out.println("Closing cache layer...");
        printStatistics();
        clearAll();
        System.out.println("Cache layer closed");
    }
}
