-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL
);

-- Departments Table
CREATE TABLE IF NOT EXISTS departments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(100),
    manager_id INT
);

-- Employees Table
CREATE TABLE IF NOT EXISTS employees (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    email VARCHAR(200) UNIQUE NOT NULL,
    department_id INT,
    salary DECIMAL(10,2),
    hire_date DATE,
    phone VARCHAR(50),
    address VARCHAR(500),
    name_lower VARCHAR(200) AS LOWER(name),
    FOREIGN KEY (department_id) REFERENCES departments(id)
);

-- Index on computed column for case-insensitive name searches
-- The name_lower column is automatically maintained by H2 and enables fast prefix searches
CREATE INDEX IF NOT EXISTS idx_employee_name_lower ON employees(name_lower)
