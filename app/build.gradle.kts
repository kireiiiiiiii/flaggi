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

    // Apply the 'application' plugin only if the project is not 'shared'
    if (project.name != "shared") {
        apply(plugin = "application")
        apply(plugin = "com.github.johnrengelman.shadow")
    }

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

    // Configure ShadowJar task only if the project is not 'shared'
    afterEvaluate {
        if (project.name != "shared") {
            val mapsDir = File(rootProject.projectDir, "../maps")
            tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
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

            // Ensure the 'build' task depends on the 'shadowJar' task
            tasks.named("build") {
                dependsOn(tasks.named("shadowJar"))
            }
        }
    }
}
