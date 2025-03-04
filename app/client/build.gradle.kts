// Plugins & depenencies ---------------------------------------------------------------------

plugins {
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

dependencies {
    implementation(project(":shared"))                              // Shared Flaggi library
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")     // JUnit for testing
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("com.google.protobuf:protobuf-java:3.25.1")      // TCP messages
}

// Application config ------------------------------------------------------------------------

application {
    mainClass.set("flaggi.client.App")
}

// Tasks -------------------------------------------------------------------------------------

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    enabled = true
    from(project(":shared").sourceSets["main"].output)
    archiveBaseName.set("Flaggi-client")
    archiveVersion.set("1.0.0")
    archiveClassifier.set("")
    destinationDirectory.set(file("$rootDir/shadowjar"))
    doLast {
        println("Client Shadow JAR has been created at: ${archiveFile.get().asFile.absolutePath}")
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
