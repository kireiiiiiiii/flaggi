#!/bin/bash

# Set default TERM value if not set
export TERM=${TERM:-xterm}

# Shell scripts explained at -> https://medium.com/@wujido20/handling-flags-in-bash-scripts-4b06b4d0ed04

#############
# VARIABLES #
#############

# Directories
ROOT=$(cd "$(dirname "$0")/.." && pwd)
SCRIPTS_DIR="$ROOT/scripts"
CLIENT_DIR="$ROOT/client"

# Resources
ABOUT_LINK="https://github.com/kireiiiiiiii/flaggi"
INPUT_PATH="$CLIENT_DIR/app/build/libs"
APP_NAME="Flaggi"
SHADOWJAR_TASK="shadowjar"
JAR_NAME="Flaggi.jar"
ICONS="$ROOT/public/icons"
DIET_JRE="$ROOT/diet-jre"

# Default options
diet=true
output_type=""
output_path=""
icon=""
jpackage_path="jpackage"
jlink_path="jlink"

###########
# METHODS #
###########

# Help message print
usage() {
  echo "Usage: $0 [OPTIONS]"
  echo "Options:"
  echo " -h, --help            Display this help message"
  echo " -n, --nodiet          Package without using the diet JRE"
  echo " --jpackage <path>     Specifies the path to the jpackage executable"
  echo " --jlink <path>        Specifies the path to the jlink executable"
  exit 0
}

# Function to handle arguments
handle_options() {
  while [ $# -gt 0 ]; do
    case $1 in
    -h | --help)
      usage
      ;;
    -n | --nodiet)
      diet=false
      ;;
    --jpackage)
      shift
      if [[ -z "$1" || "$1" == -* ]]; then
        echo "Error: -jpackage requires a valid path to the jpackage executable." >&2
        usage
        exit 1
      fi
      jpackage_path="$1"
      ;;
    --jlink)
      shift
      if [[ -z "$1" || "$1" == -* ]]; then
        echo "Error: -jlink requires a valid path to the jlink executable." >&2
        usage
        exit 1
      fi
      jlink_path="$1"
      ;;
    *)
      echo "Invalid option: $1" >&2
      usage
      exit 1
      ;;
    esac
    shift
  done
}

# Check Java version
check_java_version() {
  local java_version
  java_version=$(java -version 2>&1 | awk -F[\"_] 'NR==1 {print $2}')
  if [[ "$java_version" != "1.8.0" ]]; then
    echo "Error: Java 8 is required to run this script. Current version: $java_version"
    exit 1
  fi
}

# Build the minimal JRE
build_minimal_jre() {
  cd "$PROJECT_ROOT"

  # Delete existing JRE if it exists
  if [ -d "$DIET_JRE" ]; then
    rm -rf "$DIET_JRE"
  fi

  # Use jlink to create the JRE
  echo "Creating minimal JRE..."

  "$jlink_path" \
    --add-modules java.base,java.desktop \
    --output "$DIET_JRE" \
    --no-header-files

  echo "JRE created at $DIET_JRE"
}

###############
# MAIN SCRIPT #
###############

set -e
check_java_version

# Handle options passed to the script
handle_options "$@"

# Determine output format and icon based on OS
case "$OSTYPE" in
darwin*)
  output_type="dmg"
  output_path="$CLIENT_DIR/app/build/mac"
  icon="$ICONS/mac.icns"
  ;;
msys* | cygwin*)
  output_type="exe"
  output_path="$CLIENT_DIR/app/build/win"
  icon="$ICONS/win.ico"
  ;;
*)
  echo "Unsupported OS: $OSTYPE"
  exit 1
  ;;
esac

# Build the client JAR
echo "Building the client JAR..."
cd "$CLIENT_DIR"
./gradlew "app:$SHADOWJAR_TASK"

# Ensure the output directory exists
echo "Ensuring build directory exists at $output_path..."
mkdir -p "$output_path"

# Create the output package
if [ -f "$INPUT_PATH/$JAR_NAME" ]; then
  if [ "$diet" = true ]; then
    # Create the minimal JRE if diet mode is enabled
    echo "Creating minimal JRE for diet packaging..."
    cd "$SCRIPTS_DIR"
    build_minimal_jre

    # Package the app with a diet JRE
    echo "Creating $output_type with diet JRE..."
    "$jpackage_path" \
      --input "$INPUT_PATH" \
      --main-jar "$JAR_NAME" \
      --name "$APP_NAME" \
      --type "$output_type" \
      --dest "$output_path" \
      --icon "$icon" \
      --app-version 1.0 \
      --about-url "$ABOUT_LINK" \
      --runtime-image "$DIET_JRE"

  else
    # Package the app without a diet JRE
    echo "Creating $output_type without diet JRE..."
    "$jpackage_path" \
      --input "$INPUT_PATH" \
      --main-jar "$JAR_NAME" \
      --name "$APP_NAME" \
      --type "$output_type" \
      --dest "$output_path" \
      --icon "$icon" \
      --about-url "$ABOUT_LINK" \
      --app-version 1.0
  fi

  # Handle the created file based on the OS
  case "$output_type" in
  dmg)
    echo "DMG created at $output_path/$APP_NAME.dmg"
    ;;
  exe)
    echo "EXE created at $output_path/$APP_NAME.exe"
    ;;
  *)
    echo "Error: Unsupported output format ($output_type)"
    exit 1
    ;;
  esac

else
  echo "Error: JAR file not found at $INPUT_PATH/$JAR_NAME"
  exit 1
fi
