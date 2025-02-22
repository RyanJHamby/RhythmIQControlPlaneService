plugins {
    id("java")
}

group = "com.rhthymiq"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Dagger 2 Dependencies
    implementation("com.google.dagger:dagger:2.50")
    annotationProcessor("com.google.dagger:dagger-compiler:2.50") // Use annotationProcessor for Dagger

    // AWS Lambda Dependencies
    implementation("com.amazonaws:aws-lambda-java-core:1.2.3")
    implementation("com.amazonaws:aws-lambda-java-events:3.11.0")

    // Jackson for JSON (AWS Lambda uses Jackson)
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.0")

    // Logging
    implementation("org.slf4j:slf4j-simple:2.0.9")

    // Unit Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testImplementation("org.mockito:mockito-core:5.5.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}