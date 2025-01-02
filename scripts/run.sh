#!/bin/bash

###############
#  CONSTANTS  #
###############

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
PROJECT_ROOT=$(cd "$SCRIPT_DIR/.." && pwd)
CLIENT_DIR="$PROJECT_ROOT/client"
SERVER_DIR="$PROJECT_ROOT/server"
DIET_JRE="$PROJECT_ROOT/diet-jre"

CLIENT_JAR="$CLIENT_DIR/app/build/libs/Flaggi.jar"
SERVER_JAR="$SERVER_DIR/app/build/libs/Flaggi-server.jar"

###############
#  VARIABLES  #
###############

diet=true
mode=""
host_ip=""

###############
#   METHODS   #
###############

# Help message
usage() {
  echo "Usage: $0 <client|server|docker> [OPTIONS]"
  echo "Options:"
  echo "  -h, --help      Display this help message."
  echo "  -n, --nodiet    Don't use diet JRE, but a normal one."
  exit 0
}

# Handle options and arguments
handle_options() {
  local mode_count=0

  while [ $# -gt 0 ]; do
    case $1 in
    client)
      mode_count=$((mode_count + 1))
      mode="client"
      ;;
    server)
      mode_count=$((mode_count + 1))
      mode="server"
      ;;
    docker)
      mode_count=$((mode_count + 1))
      mode="docker"
      ;;
    -h | --help)
      usage
      ;;
    -n | --nodiet)
      diet=false
      ;;
    *)
      echo "Invalid option: $1" >&2
      usage
      ;;
    esac
    shift
  done

  # Check if more than one mode is specified
  if [ "$mode_count" -gt 1 ]; then
    echo "Error: Cannot specify multiple modes (client, server, docker) at once."
    usage
  fi
}

print_divider() {
  width=$(tput cols)
  printf '%*s\n' "$width" '' | tr ' ' '-'
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

  jlink \
    --add-modules java.base,java.desktop \
    --output "$DIET_JRE" \
    --no-header-files

  echo "JRE created at $DIET_JRE"
}

# Build and run application
build_and_run() {
  local app_dir=$1
  local jar_file=$2
  local app_name=$3

  # Build JAR
  echo "Building the $app_name JAR..."
  cd "$app_dir"
  ./gradlew shadowjar

  # Run the JAR
  if [ -f "$jar_file" ]; then
    if [ "$diet" = true ]; then
      echo "Making the diet JRE ..."
      cd "$SCRIPT_DIR"
      build_minimal_jre
      echo "Running the $app_name using diet JRE..."
      cd "$(dirname "$jar_file")"
      print_divider
      echo ""
      "$DIET_JRE/bin/java" -jar "$(basename "$jar_file")"
    else
      echo "Running the $app_name using global JRE..."
      cd "$(dirname "$jar_file")"
      echo ""
      print_divider
      java -jar "$(basename "$jar_file")"
    fi
  else
    echo "Error: JAR file not found at $jar_file"
    exit 1
  fi
}

# Run Docker container
run_docker() {

  # Get the host's IP address
  host_ip=$(ifconfig | grep 'inet ' | awk '/inet / {print $2}' | grep -Ev '^(127\.|::)')

  # Build server JAR
  echo "Building the $app_name JAR..."
  cd "$SERVER_DIR"
  ./gradlew shadowjar

  # Build the docker image
  cd "$PROJECT_ROOT"
  echo "Building Docker image..."
  docker build -t flaggi-server .

  # Stop and remove any existing container with the same name
  if [ "$(docker ps -aq -f name=flaggi-server)" ]; then
    docker stop flaggi-server
    docker rm flaggi-server
  fi

  # Run the docker container & expose the ports used by the server
  echo "Running the Docker container..."
  docker run \
    --name flaggi-server \
    -p 54321:54321/tcp \
    -p 54322:54322/udp \
    -e HOST_IP=$host_ip \
    flaggi-server

}

###############
# MAIN SCRIPT #
###############

set -e
handle_options "$@"

if [ -z "$mode" ]; then
  echo "Error: No mode specified. Use 'client', 'server', or 'docker'."
  usage
fi

if [ "$mode" = "client" ]; then
  build_and_run "$CLIENT_DIR" "$CLIENT_JAR" "client application"
elif [ "$mode" = "server" ]; then
  build_and_run "$SERVER_DIR" "$SERVER_JAR" "server application"
elif [ "$mode" = "docker" ]; then
  run_docker
fi
