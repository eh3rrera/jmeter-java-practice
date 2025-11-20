# JMeter Performance Testing

This directory contains JMeter performance test configuration for the Employee Management API.

## Directory Structure

```
jmeter/
├── test-script.jmx         # JMeter test plan (add your JMX file here)
├── github.properties       # Application configuration for CI/CD
├── thresholds.properties   # Performance thresholds
├── test-data/              # Test data files (e.g., CSV files)
└── scripts/
    └── run-test.sh        # Test execution script
```

## Prerequisites

- Java 25 or later
- Apache JMeter 5.6.3 or later
- Employee Management API application built (JAR file)

## Running Tests Locally

### 1. Add Your Test Plan

Place your JMeter test plan (JMX file) in this directory and name it `test-script.jmx`, or update the `TEST_PLAN` variable in `run-test.sh`.

### 2. Build the Application

```bash
cd ..
./mvnw clean package
```

### 3. Install JMeter

**macOS (Homebrew):**
```bash
brew install jmeter
```

**Linux:**
```bash
wget https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-5.6.3.tgz
tar -xzf apache-jmeter-5.6.3.tgz
sudo mv apache-jmeter-5.6.3 /opt/jmeter
```

### 4. Run the Test

```bash
cd scripts
chmod +x run-test.sh
./run-test.sh
```

The script will:
1. Start the application in the background
2. Wait for it to become healthy
3. Run the JMeter test
4. Generate results and HTML dashboard
5. Clean up the application process

### 5. View Results

Results are stored in `perf-results/<timestamp>/`:
- `results.jtl` - Raw test results
- `report/index.html` - HTML dashboard (open in browser)
- `report/statistics.json` - Summary statistics
- `app.log` - Application logs

## Test Configuration

### Environment Variables

You can customize the test execution by setting environment variables:

```bash
# Application JAR path (default: ../target/employee-management-api-1.0-SNAPSHOT.jar)
export APP_JAR="/path/to/your/app.jar"

# Application port (default: 8080)
export APP_PORT=8080

# JMeter installation path (default: /opt/jmeter)
export JMETER_HOME="/usr/local/Cellar/jmeter/5.6.3/libexec"

# Test plan path (default: ../test-script.jmx)
export TEST_PLAN="/path/to/your/test-script.jmx"

# JMeter properties file (default: ../github.properties)
export JMETER_PROPS="/path/to/your/github.properties"
```

### JMeter Properties

The `github.properties` file contains application-specific properties passed to the test plan:
- `app.server` - Application server hostname (default: localhost)
- `app.port` - Application port (default: 8080)
- `app.user_csv_file_path` - Path to CSV file with user credentials
- `app.employee_count` - Expected number of employees in database
- `app.department_count` - Expected number of departments in database

You can modify these properties or add additional ones as needed for your test requirements.

## Running Tests Manually with JMeter

If you prefer to run JMeter directly:

```bash
# Start the application
java -jar ../target/employee-management-api-1.0-SNAPSHOT.jar &

# Run JMeter test
jmeter -n -t test-script.jmx -q github.properties -l results.jtl -e -o report/

# Kill the application when done
pkill -f employee-management-api
```

## Troubleshooting

### Application fails to start

Check the application logs:
```bash
tail -f perf-results/<timestamp>/app.log
```

Common issues:
- Port 8080 already in use
- Database connection failure
- Missing dependencies

### JMeter fails to run

- Verify JMeter is installed: `jmeter --version`
- Check that JMETER_HOME points to the correct directory
- Ensure test-script.jmx exists and is valid

### Health check timeout

The script waits up to 60 seconds for the application to start. If your application needs more time:
1. Increase `HEALTH_CHECK_TIMEOUT` in `run-test.sh`
2. Check application logs for startup issues

## CI/CD Integration

This test setup is integrated with GitHub Actions. Every commit to `main` or `master` automatically:
1. Builds the application
2. Runs the performance test
3. Checks thresholds
4. Archives results
5. Commits results to `perf-history/`

See `.github/workflows/performance-test.yml` for the complete workflow.

## Next Steps

- Review the generated HTML dashboard to understand performance
- Adjust thresholds in the workflow if needed
- Add more test scenarios to your JMX file
- Configure test data in `test-data/` directory
