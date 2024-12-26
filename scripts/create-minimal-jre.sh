#!/bin/bash

# jlink explained -> https://www.youtube.com/watch?v=SRHqm9XjGOs

# Exit on errors
set -e

# Navigate to the root directory of the project
cd "$(dirname "$0")/.."

# Enviroment vars
OUTPUT_DIR="./diet-jre"

# Delete existing JRE if it exists
if [ -d "$OUTPUT_DIR" ]; then
    rm -rf "$OUTPUT_DIR"
fi

# Use jlink to create the JRE
echo "Creating minimal JRE..."

jlink \
    --add-modules java.base,java.desktop \
    --output "$OUTPUT_DIR" \
    --no-header-files

echo "JRE created at $OUTPUT_DIR"
