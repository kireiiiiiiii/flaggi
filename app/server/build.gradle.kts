plugins {
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

dependencies {
    // Shared dependencies
    implementation(project(":shared"))

    // JUnit for testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Application dependencies
    implementation("com.google.guava:guava:31.1-jre")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.0")
}

application {
    mainClass.set("flaggiserver.Server")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    enabled = true
    archiveBaseName.set("Flaggi-server")
    archiveVersion.set("1.0.0")
    archiveClassifier.set("") // Removes the "-all" suffix

    destinationDirectory.set(file("$rootDir/shadowjar"))

    doLast {
        println("Server Shadow JAR has been created at: ${archiveFile.get().asFile.absolutePath}")
    }
}
