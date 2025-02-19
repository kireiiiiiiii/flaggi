plugins {
    id("java-library")
}

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    //---- JSON file manipulation ----
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
    implementation("com.fasterxml.jackson.core:jackson-core:2.12.3")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.12.3")

    //---- JSON object for Github release fetching ----
    implementation("org.json:json:20230618")

    // Application dependencies
    implementation(libs.guava)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
