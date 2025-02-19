// VARIABLES -------------------------------------------------------------

val mainClassName = "flaggiserver.Server"
val projectName = "Flaggi-server"

// DEPENDENCIES ----------------------------------------------------------

plugins {
    application
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
}

dependencies {
    // JUnit for testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Application dependencies
    implementation("com.google.guava:guava:31.1-jre")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.0")
}

// JAVA ------------------------------------------------------------------

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

// BUILD -----------------------------------------------------------------

application {
    mainClass.set(mainClassName)
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed", "skipped")
        showStandardStreams = true
    }
}

tasks.shadowJar {
    mergeServiceFiles()
    archiveClassifier.set("")
    archiveFileName.set(projectName + ".jar")

    // Inluce the maps data in the jar
    val mapsDir = File(rootProject.projectDir, "../maps")
    if (!mapsDir.exists()) {
        throw GradleException("Build failed: Maps directory not found at ${mapsDir.absolutePath}")
    }
    from(mapsDir) {
        into("maps")
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.jar {
    enabled = false
}
