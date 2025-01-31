////////////////
// VARIABLES ///
////////////////

val mainClassName = "flaggieditor.App"
val projectName = "flaggi-map-editor"

///////////////////
// BUILD SCRIPT ///
///////////////////

plugins {
    id("application")
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
} 

repositories {
    mavenCentral()
}

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // This dependency is used by the application.
    implementation(libs.guava)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8)) // Use Java 8
    }
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

application {
    mainClass.set(mainClassName)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed", "skipped")
        showStandardStreams = true
    }
}

tasks.processResources {
    from("../LICENSES") {
        into("licenses")
    }
}

tasks.shadowJar {
    mergeServiceFiles()
    archiveClassifier.set("")
    archiveFileName.set(projectName + ".jar")
}
