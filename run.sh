#!/bin/bash
# Bill Payment System - CLI Entry Point
#
# Interactive shell mode (state persists between commands):
#   ./run.sh
#   > CASH_IN 1000000
#   > LIST_BILL
#   > EXIT
#
# Single-command mode:
#   ./run.sh CASH_IN 1000000

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR="$SCRIPT_DIR/target/bill-payment.jar"

# Build if JAR doesn't exist
if [ ! -f "$JAR" ]; then
    echo "Building project..."
    cd "$SCRIPT_DIR" && mvn -q clean package -DskipTests
    if [ $? -ne 0 ]; then
        echo "Build failed. Please check your Java/Maven installation."
        exit 1
    fi
fi

java -jar "$JAR" "$@"

