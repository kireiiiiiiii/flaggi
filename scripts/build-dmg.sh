#!/bin/bash

# Exit on errors
set -e

# Navigate to the root directory of the project
cd "$(dirname "$0")/.."

# Define paths
APP_NAME="Flaggi"
JAR_PATH="app/build/libs/Flaggi.jar"
BUILD_DIR="app/build/mac"
SHADOWJAR_TASK="shadowJar"

# Step 1: Build the JAR with Gradle
echo "Building JAR with Gradle..."
./gradlew :app:$SHADOWJAR_TASK

# Step 2: Create the output directory if it doesn't exist
echo "Ensuring build directory exists at $BUILD_DIR..."
mkdir -p "$BUILD_DIR"

# Step 3: Use jpackage to create the DMG
echo "Creating DMG..."
jpackage \
  --input app/build/libs \
  --main-jar Flaggi.jar \
  --name "$APP_NAME" \
  --type dmg \
  --dest "$BUILD_DIR" \
  --icon app/src/main/resources/icon-mac.icns \
  --app-version 1.0

echo "DMG created at $BUILD_DIR/$APP_NAME.dmg"
