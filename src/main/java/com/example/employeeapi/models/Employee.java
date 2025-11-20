package com.example.employeeapi.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Employee {
    private int id;
    private String name;
    private String email;
    private Integer departmentId;
    private BigDecimal salary;
    private LocalDate hireDate;
    private String phone;
    private String address;

    public Employee() {
    }

    public Employee(int id, String name, String email, Integer departmentId,
                   BigDecimal salary, LocalDate hireDate, String phone, String address) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.departmentId = departmentId;
        this.salary = salary;
        this.hireDate = hireDate;
        this.phone = phone;
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
