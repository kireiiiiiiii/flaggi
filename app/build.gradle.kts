plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "application")
    apply(plugin = "com.github.johnrengelman.shadow")

    configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(8))
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "failed", "skipped")
            showStandardStreams = true
        }
    }

    tasks.withType<Jar> {
        enabled = false
    }

    afterEvaluate {
        val mapsDir = File(rootProject.projectDir, "../maps")
        if (!mapsDir.exists()) {
            throw GradleException("Build failed: Maps directory not found at ${mapsDir.absolutePath}")
        }
        tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
            enabled = true
            mergeServiceFiles()
            archiveClassifier.set("")
            destinationDirectory.set(file("$rootDir/shadowjar"))

            doLast {
                println("Shadow JAR has been created at: ${archiveFile.get().asFile.absolutePath}")
            }

            from(mapsDir) {
                into("maps")
            }
        }
    }
}
