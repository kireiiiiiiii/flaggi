#!/bin/bash

# Exit on errors
set -e

# Determine project root directory based on script location
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
PROJECT_ROOT=$(cd "$SCRIPT_DIR/.." && pwd)
JAR_FILE="$PROJECT_ROOT/app/build/libs/Flaggi.jar"

# Step 1: Build the JAR
echo "Building the client JAR..."
cd "$PROJECT_ROOT"
./gradlew shadowjar

# Step 2: Run the JAR
if [ -f "$JAR_FILE" ]; then
    echo "Running the client application..."
    cd "$(dirname "$JAR_FILE")"
    java -jar "$(basename "$JAR_FILE")"
else
    echo "Error: JAR file not found at $JAR_FILE"
    exit 1
fi
