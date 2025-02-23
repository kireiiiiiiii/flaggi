// Plugins & depenencies ---------------------------------------------------------------------

plugins {
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

dependencies {
    // Shared Flaggi library
    implementation(project(":shared"))

    // JUnit for testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // JSON dependencies
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.0")
}

// Application config ------------------------------------------------------------------------

application {
    mainClass.set("flaggi.server.Server")
}

// Tasks -------------------------------------------------------------------------------------

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    enabled = true
    from(project(":shared").sourceSets["main"].output)
    archiveBaseName.set("Flaggi-server")
    archiveVersion.set("1.0.0")
    archiveClassifier.set("")
    destinationDirectory.set(file("$rootDir/shadowjar"))
    doLast {
        println("Server Shadow JAR has been created at: ${archiveFile.get().asFile.absolutePath}")
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
