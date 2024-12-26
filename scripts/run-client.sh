#!/bin/bash

# Shell scripts explained at -> https://medium.com/@wujido20/handling-flags-in-bash-scripts-4b06b4d0ed04

# Variables
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
PROJECT_ROOT=$(cd "$SCRIPT_DIR/.." && pwd)
CLIENT_DIR="$PROJECT_ROOT/client"
JAR_FILE="$CLIENT_DIR/app/build/libs/Flaggi.jar"
DIET_JRE="$PROJECT_ROOT/diet-jre"
diet=false

# Help message print
usage() {
 echo "Usage: $0 [OPTIONS]"
 echo "Options:"
 echo " -h, --help      Display this help message"
 echo " -d, --diet      Make and use a died version of the JRE."
}

# Has arguments?
has_argument() {
    [[ ("$1" == *=* && -n ${1#*=}) || ( ! -z "$2" && "$2" != -*)  ]];
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

# Buld the client JAR
echo "Building the client JAR..."
cd "$CLIENT_DIR"
./gradlew shadowjar

# Run the JAR
if [ -f "$JAR_FILE" ]; then

    if [ "$diet" = true ]; then
        echo "Making the diet JRE ..."
        cd "$SCRIPT_DIR"
        ./make-diet-jre.sh
        echo "Running the client application using diet JRE..."
        cd "$(dirname "$JAR_FILE")"
        "$DIET_JRE/bin/java" -jar "$(basename "$JAR_FILE")"

    else 
        echo "Running the client application using global JRE..."
        cd "$(dirname "$JAR_FILE")"
        java -jar "$(basename "$JAR_FILE")"
    fi

else
    echo "Error: JAR file not found at $JAR_FILE"
    exit 1
fi
