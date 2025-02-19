plugins {
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

dependencies {

    // Shared dependencies
    implementation(project(":shared"))

    // Use JUnit Jupiter for testing.
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Application dependencies
    implementation(libs.guava)

    //---- JSON file manipulation ----
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.0")
}

application {
    mainClass.set("flaggieditor.App")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
