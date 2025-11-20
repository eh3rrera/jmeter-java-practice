# Employee Management API

A REST API for employee management built with Java SE 25's built-in `HttpServer`. This application is designed for performance testing demonstrations with Apache JMeter. This version uses virtual threads instead of platform threads, pooled connections instead of direct connections to the database, adds an index to the name (lowercase) for the employees table, and implements caching for department and employee records.

## Technology Stack

- **Java Version:** Java SE 25
- **HTTP Server:** `com.sun.net.httpserver.HttpServer` (built-in, no external frameworks)
- **Database:** H2 Database in server mode
- **Build Tools:** Maven or Gradle
- **JSON Processing:** Gson
- **Password Hashing:** BCrypt
- **JWT Tokens:** JJWT

## Project Structure

```
employee-management-api/
├── pom.xml
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
├── gradle/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── example/
│       │           └── employeeapi/
│       │               ├── Main.java
│       │               ├── server/
│       │               │   └── HttpServerSetup.java
│       │               ├── handlers/
│       │               │   ├── LoginHandler.java
│       │               │   ├── EmployeeHandler.java
│       │               │   ├── DepartmentHandler.java
│       │               │   └── ReportHandler.java
│       │               ├── cache/
│       │               │   └── CacheManager.java
│       │               ├── dao/
│       │               │   ├── CachedDepartmentDAO.java
│       │               │   ├── CachedEmployeeDAO.java
│       │               │   ├── UserDAO.java
│       │               │   ├── EmployeeDAO.java
│       │               │   └── DepartmentDAO.java
│       │               ├── models/
│       │               │   ├── User.java
│       │               │   ├── Employee.java
│       │               │   └── Department.java
│       │               ├── database/
│       │               │   └── DatabaseManager.java
│       │               └── util/
│       │                   ├── ConfigUtil.java
│       │                   ├── JsonUtil.java
│       │                   ├── LocalDateAdapter.java
│       │                   └── JwtUtil.java
│       └── resources/
│           ├── application.properties
│           ├── schema.sql
│           └── data.sql
├── data/
│   └── (H2 database files)
└── README.md
```

## Building the Project

### Using Maven

```bash
mvn clean package
```

This will create a fat JAR file in the `target` directory: `employee-management-api-1.0-SNAPSHOT.jar`

### Using Gradle

```bash
./gradlew clean shadowJar
```

This will create a fat JAR file in the `build/libs` directory: `employee-management-api-1.0-SNAPSHOT.jar`

## Running the Application

### Using the JAR file

With Maven:
```bash
java -jar target/employee-management-api-1.0-SNAPSHOT.jar
```

With Gradle:
```bash
java -jar build/libs/employee-management-api-1.0-SNAPSHOT.jar
```

### During Development

With Maven:
```bash
mvn exec:java -Dexec.mainClass="com.example.employeeapi.Main"
```

With Gradle:
```bash
./gradlew run
```

The server will start on `http://localhost:8080` (or the port configured in `application.properties`)

## Configuration

The application can be configured via `src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=8080

# Database Configuration
db.url=jdbc:h2:tcp://localhost:9092/./data/employeedb
db.username=sa
db.password=

# Thread Pool Configuration (deprecated, now uses virtual threads)
server.thread.pool.size=100

# Data Generation Configuration
data.generator.employee.count=500000

# Connection Pool Configuration
db.pool.size=20
db.pool.connection.timeout=30000
db.pool.idle.timeout=600000
db.pool.max.lifetime=1800000

# Cache Configuration
# Employee cache: 500k employees, cache hot 10% (~50-100MB memory)
cache.employee.ttl.minutes=10
cache.employee.max.size=50000

# Department cache: 5 departments total (all fit in cache)
cache.department.ttl.minutes=30
cache.department.max.size=10

# Department list cache: single entry for findAll()
cache.department.list.ttl.minutes=30

# Search results cache
cache.search.ttl.minutes=10
cache.search.max.size=5000
```

You can modify these values to change the server port, database connection, thread pool size, connection pool configuration, or caching configuration.

## Database Information

- **Connection URL:** `jdbc:h2:tcp://localhost:9092/./data/employeedb`
- **Username:** `sa`
- **Password:** (empty)
- **H2 Console:** You can connect to the database using any SQL client

The application automatically:
1. Starts the H2 database server on port 9092
2. Creates tables from `schema.sql` if they don't exist
3. Inserts sample data from `data.sql` (10 users, 5 departments)
4. Generates employee data to insert into the database. The number of inserted records is given by the property `data.generator.employee.count`

### Database Schema Files

The database schema and sample data are externalized in SQL files:

- **`src/main/resources/schema.sql`** - DDL statements to create tables (users, departments, employees)
- **`src/main/resources/data.sql`** - DML statements to insert sample users and departments

Note: User passwords are pre-hashed using BCrypt and stored in `data.sql`.

## Test Users

| Username | Password |
|----------|----------|
| user01   | user01   |
| user02   | user02   |
| user03   | user03   |
...
| user99   | user99   |

## API Endpoints

**Important:** All endpoints except `/api/login` require JWT authentication via the `Authorization: Bearer <token>` header.

### Authentication

#### POST /api/login
Login and receive a JWT token (no authentication required).

**Request:**
```bash
curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user01","password":"user01"}'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresIn": 3600
}
```

### Employee Endpoints

All employee endpoints require the `Authorization: Bearer <token>` header.

#### GET /api/employees/{id}
Get a single employee by ID.

**Request:**
```bash
curl http://localhost:8080/api/employees/1 \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**Response:**
```json
{
  "id": 1,
  "name": "John Smith",
  "email": "john.smith@company.com",
  "departmentId": 1,
  "salary": 85000.00,
  "hireDate": "2020-01-15",
  "phone": "555-0101",
  "address": "123 Main St, City, State 12345"
}
```

#### GET /api/employees/search?name={name}
Search employees by name (case-insensitive).

**Request:**
```bash
curl "http://localhost:8080/api/employees/search?name=john" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**Response:**
```json
[
  {
    "id": 1,
    "name": "John Smith",
    "email": "john.smith@company.com",
    ...
  },
  {
    "id": 3,
    "name": "Michael Johnson",
    "email": "michael.j@company.com",
    ...
  }
]
```

### Department Endpoints

All department endpoints require the `Authorization: Bearer <token>` header.

#### GET /api/departments
Get all departments.

**Request:**
```bash
curl http://localhost:8080/api/departments \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**Response:**
```json
[
  {
    "id": 1,
    "name": "Engineering",
    "location": "Building A",
    "managerId": null
  },
  ...
]
```

#### GET /api/departments/{id}/employees
Get all employees in a specific department.

**Request:**
```bash
curl http://localhost:8080/api/departments/1/employees \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**Response:**
```json
[
  {
    "id": 1,
    "name": "John Smith",
    "email": "john.smith@company.com",
    "departmentId": 1,
    ...
  },
  ...
]
```

### Report Endpoints (with Simulated I/O Delays)

All report endpoints require the `Authorization: Bearer <token>` header.

These endpoints include simulated external service calls using `Thread.sleep()` for performance testing demonstrations.

#### GET /api/reports/employee-profile/{id}
Get comprehensive employee profile with simulated external service calls.

**Simulated delays:**
- Background verification service: 200ms
- Payroll system query: 150ms

**Request:**
```bash
curl http://localhost:8080/api/reports/employee-profile/1 \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**Response:**
```json
{
  "employee": { ... },
  "department": { ... },
  "backgroundVerificationStatus": "Verified",
  "payrollStatus": "Active",
  "departmentAverageSalary": 88166.67
}
```

#### GET /api/reports/department-analytics/{id}
Get department analytics with simulated external service calls.

**Simulated delays:**
- HR system integration: 300ms
- Benefits provider API: 200ms

**Request:**
```bash
curl http://localhost:8080/api/reports/department-analytics/1 \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**Response:**
```json
{
  "department": { ... },
  "salaryStatistics": {
    "minSalary": 78000.00,
    "maxSalary": 95000.00,
    "averageSalary": 88166.67,
    "employeeCount": 6
  },
  "employeesByHireYear": {
    "2018": 1,
    "2019": 2,
    "2020": 2,
    "2021": 1
  },
  "hrSystemStatus": "Connected",
  "benefitsProviderStatus": "Active"
}
```

#### GET /api/dashboard/company-overview
Get company-wide statistics with multiple simulated external service calls.

**Simulated delays:**
- Financial system API: 250ms
- Compliance check service: 200ms
- Attendance system: 150ms

**Request:**
```bash
curl http://localhost:8080/api/dashboard/company-overview \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**Response:**
```json
{
  "totalEmployees": 30,
  "totalDepartments": 5,
  "averageSalary": 72066.67,
  "financialSystemStatus": "Operational",
  "complianceStatus": "Compliant",
  "attendanceSystemStatus": "Online"
}
```

## Performance Testing Notes

### Current Implementation

This version is intentionally designed with performance bottlenecks for educational purposes:

2. **Simulated I/O Delays:** Report endpoints use `Thread.sleep()` to simulate blocking external service calls

### Request Logging

All requests are logged with the format:
```
[METHOD] /path - Status: 200 - Duration: 123ms
```

## Stopping the Server

Press `Ctrl+C` to gracefully shut down the server. The shutdown hook will:
1. Stop the HTTP server
2. Stop the H2 database server
3. Close all connections

## License

This is a demo application for educational purposes.
