#!/bin/bash

# Exit on errors
set -e

# Navigate to the root directory of the project
cd "$(dirname "$0")/.."

# Define paths
APP_NAME="Flaggi"
JAR_PATH="app/build/libs/Flaggi.jar"
BUILD_DIR="app/build/win"
SHADOWJAR_TASK="shadowJar"

# Step 1: Build the JAR with Gradle
echo "Building JAR with Gradle..."
./gradlew :app:$SHADOWJAR_TASK

# Step 2: Create the output directory if it doesn't exist
echo "Ensuring build directory exists at $BUILD_DIR..."
mkdir -p "$BUILD_DIR"

# Step 3: Use jpackage to create the EXE
echo "Creating EXE..."
jpackage \
  --input app/build/libs \
  --main-jar Flaggi.jar \
  --name "$APP_NAME" \
  --type exe \
  --dest "$BUILD_DIR" \
  --win-menu \
  --win-dir-chooser \
  --win-shortcut \
  --icon app/src/main/resources/icon-win.ico \
  --app-version 1.0

echo "EXE created at $BUILD_DIR/$APP_NAME.exe"
