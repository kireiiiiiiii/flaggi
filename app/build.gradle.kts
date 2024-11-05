import java.io.FileOutputStream

///////////////
// VARIABLES ///
///////////////////

var main_class = "kireiiiiiiii.App"

///////////////////
// BUILD SCRIPT ///
///////////////////

plugins {
    id ("application")
    id ("java")
    id ("com.github.johnrengelman.shadow") version "8.1.1"
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
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = main_class
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.shadowJar {
    mergeServiceFiles()
    archiveClassifier.set("")
}
