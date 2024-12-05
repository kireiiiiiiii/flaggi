#!/bin/bash

# Exit on errors
set -e

# Determine project root directory based on script location
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
PROJECT_ROOT=$(cd "$SCRIPT_DIR/.." && pwd)
SERVER_DIR="$PROJECT_ROOT/server"
BUILD_DIR="$SERVER_DIR/build"
CLASSES_DIR="$BUILD_DIR/classes"
JAR_FILE="$BUILD_DIR/libs/Server.jar"
MANIFEST_FILE="$SERVER_DIR/MANIFEST.MF"

# Step 1: Prepare build directories
echo "Setting up build directories..."
mkdir -p "$CLASSES_DIR"
mkdir -p "$(dirname "$JAR_FILE")"

# Step 2: Compile the server code
echo "Compiling the server application..."
cd "$SERVER_DIR"
javac -d "$CLASSES_DIR" Server.java

# Step 3: Create the JAR
if [ -f "$MANIFEST_FILE" ]; then
    echo "Packaging the server JAR..."
    jar cfm "$JAR_FILE" "$MANIFEST_FILE" -C "$CLASSES_DIR" .
else
    echo "Error: Manifest file not found at $MANIFEST_FILE"
    exit 1
fi

# Step 4: Run the server JAR
if [ -f "$JAR_FILE" ]; then
    echo "Running the server application..."
    java -jar "$JAR_FILE"
else
    echo "Error: JAR file not found at $JAR_FILE"
    exit 1
fi
