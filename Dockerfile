# Use Java 8
FROM openjdk:8-jdk

# Set the working directory in the container to the server directory
WORKDIR /app/server

# Install wget, unzip (needed to install Gradle), and iproute2 (for network tools)
RUN apt-get update && apt-get install -y wget unzip iproute2

# Install Gradle 8.10.2
RUN wget https://services.gradle.org/distributions/gradle-8.10.2-bin.zip -P /tmp \
    && unzip /tmp/gradle-8.10.2-bin.zip -d /opt \
    && ln -s /opt/gradle-8.10.2/bin/gradle /usr/bin/gradle

# Copy the server project files & run script into the container
COPY server /app/server
COPY scripts/run-server.sh /app/scripts/run-server.sh

# Make the run-server.sh script executable
RUN chmod +x /app/scripts/run-server.sh

# Expose the ports for the server
EXPOSE 54321 54322

# Run the run-server.sh script
CMD ["/app/scripts/run-server.sh"]
