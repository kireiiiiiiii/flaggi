#!/bin/bash

# Shell scripts explained at -> https://medium.com/@wujido20/handling-flags-in-bash-scripts-4b06b4d0ed04

#############
# VARIABLES #
#############

# Directories
ROOT=$(cd "$(dirname "$0")/.." && pwd)
SCRIPTS_DIR="$ROOT/scripts"
CLIENT_DIR="$ROOT/client"

# Resources
INPUT_PATH="$CLIENT_DIR/app/build/libs"
OUTPUT_PATH="$CLIENT_DIR/app/build/mac"

APP_NAME="Flaggi"
SHADOWJAR_TASK="shadowjar"
JAR_NAME="Flaggi.jar"
ICONS="$ROOT/public/icons"
DIET_JRE="$ROOT/diet-jre"

# Variables
diet=false
output=""
icon=""

###########
# METHODS #
###########

# Help message print
usage() {
  echo "Usage: $0 [OPTIONS]"
  echo "Options:"
  echo " -h, --help      Display this help message"
  echo " -d, --diet      Package with a diet version of the JRE."
}

# Has arguments?
has_argument() {
  [[ ("$1" == *=* && -n ${1#*=}) || (! -z "$2" && "$2" != -*) ]]
}

# Get the argument
extract_argument() {
  echo "${2:-${1#*=}}"
}

# Function to handle options and arguments
handle_options() {
  while [ $# -gt 0 ]; do
    case $1 in
    -h | --help)
      usage
      exit 0
      ;;
    -d | --diet)
      diet=true
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

###############
# MAIN SCRIPT #
###############

set -e
handle_options "$@"

# Get output kind based on OS.
case "$OSTYPE" in
darwin*)
  output="dmg"
  icon="$ICONS/mac.icns"
  ;;
msys*)
  output="exe"
  icon="$ICONS/win.ico"
  ;;
*)
  echo "Unsupported OS: $OSTYPE"
  exit 1
  ;;
esac

# Buld the client JAR
echo "Building the client JAR..."
cd "$CLIENT_DIR"
./gradlew "app:$SHADOWJAR_TASK"

# Create the output directory if it doesn't exist
echo "Ensuring build directory exists at $OUTPUT_PATH..."
mkdir -p "$OUTPUT_PATH"

# Run the JAR
if [ -f "$INPUT_PATH/$JAR_NAME" ]; then

  if [ "$diet" = true ]; then
    cd "$SCRIPTS_DIR"
    ./create-minimal-jre.sh
    echo "Creating DMG with diet JRE..."
    jpackage \
      --input "$INPUT_PATH" \
      --main-jar "$JAR_NAME" \
      --name "$APP_NAME" \
      --type "$output" \
      --dest "$OUTPUT_PATH" \
      --icon "$icon" \
      --app-version 1.0 \
      --runtime-image "$DIET_JRE"

  else
    echo "Creating DMG..."
    jpackage \
      --input "$INPUT_PATH" \
      --main-jar "$JAR_NAME" \
      --name "$APP_NAME" \
      --type "$output" \
      --dest "$OUTPUT_PATH" \
      --icon "$icon" \
      --app-version 1.0

  fi

else
  echo "Error: JAR file not found at $INPUT_PATH/$JAR_NAME"
  exit 1
fi

echo "DMG created at $OUTPUT_PATH/$APP_NAME.dmg"
