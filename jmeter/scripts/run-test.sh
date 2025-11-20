#!/usr/bin/env bash

# run-test.sh
# Orchestrates JMeter performance test execution for CI/CD
#
# This script:
# 1. Starts the Java application in the background
# 2. Waits for the app to become healthy
# 3. Runs JMeter test with timestamped results
# 4. Cleans up the application process
# 5. Returns JMeter's exit code
#
# Usage: ./run-test.sh
# Environment variables:
#   APP_JAR - Path to application JAR (default: ../target/employee-management-api-1.0-SNAPSHOT.jar)
#   APP_PORT - Application port (default: 8080)
#   JMETER_HOME - Path to JMeter installation (default: /opt/jmeter)
#   TEST_PLAN - Path to JMX file (default: ../test-script.jmx)
#   JMETER_PROPS - Path to properties file (default: ../github.properties)

set -euo pipefail

# Configuration
APP_JAR="${APP_JAR:-../../target/employee-management-api-1.0-SNAPSHOT.jar}"
APP_PORT="${APP_PORT:-8080}"
JMETER_HOME="${JMETER_HOME:-/Users/eh/Documents/apache-jmeter-5.6.3}"
#JMETER_HOME="${JMETER_HOME:-/opt/jmeter}"
TEST_PLAN="${TEST_PLAN:-../test-script.jmx}"
JMETER_PROPS="${JMETER_PROPS:-../github.properties}"
HEALTH_CHECK_URL="http://localhost:${APP_PORT}/api/login"
HEALTH_CHECK_TIMEOUT=60
HEALTH_CHECK_INTERVAL=2

# Timestamp for results
TIMESTAMP=$(date +%Y-%m-%d-%H%M%S)
RESULTS_DIR="../../perf-results/${TIMESTAMP}"
RESULTS_FILE="${RESULTS_DIR}/results.jtl"
REPORT_DIR="${RESULTS_DIR}/report"

# Track application PID for cleanup
APP_PID=""

# Cleanup function - kills app if still running
cleanup() {
    local exit_code=$?
    echo ""
    echo "Cleaning up..."

    if [ -n "$APP_PID" ] && kill -0 "$APP_PID" 2>/dev/null; then
        echo "Stopping application (PID: $APP_PID)..."
        kill "$APP_PID" 2>/dev/null || true
        # Wait a bit for graceful shutdown
        sleep 2
        # Force kill if still running
        if kill -0 "$APP_PID" 2>/dev/null; then
            echo "Force stopping application..."
            kill -9 "$APP_PID" 2>/dev/null || true
        fi
    fi

    echo "Cleanup complete"
    exit $exit_code
}

# Register cleanup trap
trap cleanup EXIT INT TERM

echo "========================================="
echo "JMeter Performance Test Runner"
echo "========================================="
echo "Configuration:"
echo "  Application JAR: $APP_JAR"
echo "  Application Port: $APP_PORT"
echo "  JMeter Home: $JMETER_HOME"
echo "  Test Plan: $TEST_PLAN"
echo "  Properties: $JMETER_PROPS"
echo "  Results Directory: $RESULTS_DIR"
echo "========================================="
echo ""

# Validate required files exist
if [ ! -f "$APP_JAR" ]; then
    echo "ERROR: Application JAR not found: $APP_JAR"
    exit 1
fi

if [ ! -f "$TEST_PLAN" ]; then
    echo "ERROR: Test plan not found: $TEST_PLAN"
    exit 1
fi

if [ ! -f "$JMETER_PROPS" ]; then
    echo "WARNING: Properties file not found: $JMETER_PROPS (will use JMeter defaults)"
fi

if [ ! -x "${JMETER_HOME}/bin/jmeter" ]; then
    echo "ERROR: JMeter not found or not executable: ${JMETER_HOME}/bin/jmeter"
    exit 1
fi

# Create results directory
mkdir -p "$RESULTS_DIR"
echo "Created results directory: $RESULTS_DIR"
echo ""

# Step 1: Start the application
echo "Step 1: Starting application..."
java -jar "$APP_JAR" > "${RESULTS_DIR}/app.log" 2>&1 &
APP_PID=$!
echo "Application started with PID: $APP_PID"
echo "Application logs: ${RESULTS_DIR}/app.log"
echo ""

# Step 2: Wait for application to become healthy
echo "Step 2: Waiting for application to become healthy..."
echo "Health check URL: $HEALTH_CHECK_URL"

ELAPSED=0
while [ $ELAPSED -lt $HEALTH_CHECK_TIMEOUT ]; do
    if curl -sf "$HEALTH_CHECK_URL" \
           -H "Content-Type: application/json" \
           -d '{"username":"user01","password":"user01"}' \
           > /dev/null 2>&1; then
        echo "✓ Application is healthy (took ${ELAPSED}s)"
        break
    fi

    # Check if app process is still running
    if ! kill -0 "$APP_PID" 2>/dev/null; then
        echo "ERROR: Application process terminated unexpectedly"
        echo "Last 20 lines of application log:"
        tail -n 20 "${RESULTS_DIR}/app.log"
        exit 1
    fi

    echo "  Waiting for app to start... (${ELAPSED}/${HEALTH_CHECK_TIMEOUT}s)"
    sleep $HEALTH_CHECK_INTERVAL
    ELAPSED=$((ELAPSED + HEALTH_CHECK_INTERVAL))
done

if [ $ELAPSED -ge $HEALTH_CHECK_TIMEOUT ]; then
    echo "ERROR: Application failed to become healthy within ${HEALTH_CHECK_TIMEOUT}s"
    echo "Last 20 lines of application log:"
    tail -n 20 "${RESULTS_DIR}/app.log"
    exit 1
fi

echo ""

# Step 3: Run JMeter test
echo "Step 3: Running JMeter performance test..."
echo "Test started at: $(date)"

JMETER_CMD="${JMETER_HOME}/bin/jmeter"
JMETER_ARGS="-n -t $TEST_PLAN -l $RESULTS_FILE -e -o $REPORT_DIR"

# Add properties file if it exists
if [ -f "$JMETER_PROPS" ]; then
    JMETER_ARGS="$JMETER_ARGS -q $JMETER_PROPS"
fi

echo "Command: $JMETER_CMD $JMETER_ARGS"
echo ""

# Run JMeter and capture its exit code
set +e
$JMETER_CMD $JMETER_ARGS
JMETER_EXIT_CODE=$?
set -e

echo ""
echo "Test completed at: $(date)"
echo "JMeter exit code: $JMETER_EXIT_CODE"
echo ""

# Step 4: Report results location
echo "========================================="
echo "Test Results"
echo "========================================="
echo "Results file: $RESULTS_FILE"
echo "HTML Report: $REPORT_DIR/index.html"
echo "Statistics: $REPORT_DIR/statistics.json"
echo ""

if [ $JMETER_EXIT_CODE -eq 0 ]; then
    echo "✓ JMeter test completed successfully"
else
    echo "✗ JMeter test failed with exit code: $JMETER_EXIT_CODE"
fi

echo "========================================="

# Cleanup will be triggered by trap
exit $JMETER_EXIT_CODE
