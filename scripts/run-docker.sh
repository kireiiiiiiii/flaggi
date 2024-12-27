#!/bin/bash

# Exit on errors
set -e

# Variables
ROOT_DIR="$(dirname "$0")/.."

# Extract Host IP Address
HOST_IP=$(ifconfig | grep 'inet ' | awk '/inet / {print $2}' | grep -Ev '^(127\.|::)')

# Navigate to the root directory
cd "$ROOT_DIR"

# Build the docker image
docker build -t flaggi-server .

# Stop and remove any existing container with the same name
if [ "$(docker ps -aq -f name=flaggi-server)" ]; then
    docker stop flaggi-server
    docker rm flaggi-server
fi

# Run the docker container & expose the ports used by the server
docker run \
    --name flaggi-server \
    -p 54321:54321/tcp \
    -p 54322:54322/udp \
    -e HOST_IP=$HOST_IP \
    flaggi-server