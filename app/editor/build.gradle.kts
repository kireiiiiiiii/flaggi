// Plugins & depenencies ---------------------------------------------------------------------

plugins {
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

dependencies {
    // Shared dependencies
    implementation(project(":shared"))

    // JUnit for testing
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // JSON file manipulation
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.0")
}

// Application config ------------------------------------------------------------------------

application {
    mainClass.set("flaggieditor.App")
}

// Tasks -------------------------------------------------------------------------------------

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    enabled = true
    from(project(":shared").sourceSets["main"].output)
    archiveBaseName.set("Flaggi-editor")
    archiveVersion.set("1.0.0")
    archiveClassifier.set("")
    destinationDirectory.set(file("$rootDir/shadowjar"))
    doLast {
        println("Editor Shadow JAR has been created at: ${archiveFile.get().asFile.absolutePath}")
    }
}

tasks.named<JavaExec>("run") {
    dependsOn(tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar"))
    classpath = sourceSets["main"].runtimeClasspath + files(tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar").get().archiveFile)
    mainClass.set(application.mainClass)
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
