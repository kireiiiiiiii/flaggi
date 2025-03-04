// Plugins & dependencies ---------------------------------------------------------------------

plugins {
    id("java-library")
    id("com.google.protobuf") version "0.9.4"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit.jupiter)                               // JUnit for testing
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.0") // JSON file manipulation
    implementation("com.google.protobuf:protobuf-java:3.25.1")           // TCP messages
    implementation("org.json:json:20230618")                             // GitHub API client
}

// Protobuf -----------------------------------------------------------------------------------

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.1"
    }
    generateProtoTasks {
        all().configureEach {
            builtins {
                named("java")
            }
        }
    }
}
