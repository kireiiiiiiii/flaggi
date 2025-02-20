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

    // JSON file manipulation
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.0")
}

application {
    mainClass.set("flaggieditor.App")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    enabled = true
    from(project(":shared").sourceSets["main"].output)
    archiveBaseName.set("Flaggi-editor")
    archiveVersion.set("1.0.0")
    archiveClassifier.set("") // Removes the "-all" suffix

    destinationDirectory.set(file("$rootDir/shadowjar"))

    doLast {
        println("Editor Shadow JAR has been created at: ${archiveFile.get().asFile.absolutePath}")
    }
}
