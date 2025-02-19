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

    // JSON file manipulation
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
    implementation("com.fasterxml.jackson.core:jackson-core:2.12.3")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.12.3")

    // JSON object for Github release fetching
    implementation("org.json:json:20230618")

    // Application dependencies
    implementation(libs.guava)
}

application {
    mainClass.set("flaggi.App")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    enabled = true
    archiveBaseName.set("Flaggi-client")
    archiveVersion.set("1.0.0")
    archiveClassifier.set("") // Removes the "-all" suffix

    destinationDirectory.set(file("$rootDir/shadowjar"))

    doLast {
        println("Client Shadow JAR has been created at: ${archiveFile.get().asFile.absolutePath}")
    }
}
