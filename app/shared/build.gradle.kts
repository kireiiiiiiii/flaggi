// Plugins & depenencies ---------------------------------------------------------------------

plugins {
    id("java-library")
}

dependencies {
    // JUnit for testing
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // JSON file manipulation
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.0")

    implementation("org.json:json:20230618") // GitHub api client

}
