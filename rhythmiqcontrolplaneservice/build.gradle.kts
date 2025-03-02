plugins {
    id("java")
    id("idea")
    id("org.openapi.generator") version "6.0.1"
}

group = "com.rhthymiq.controlplaneservice"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    // JAX-RS API for RESTful services
    implementation("jakarta.ws.rs:jakarta.ws.rs-api:3.0.0")

    implementation("javax.annotation:javax.annotation-api:1.3.2")

    // Dagger 2 Dependencies
    implementation("com.google.dagger:dagger:2.50")
    annotationProcessor("com.google.dagger:dagger-compiler:2.50") // Use annotationProcessor for Dagger

//    implementation("com.github.julman99:gson-fire:1.8.4")
    implementation("com.google.code.gson:gson:2.8.8")

    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    // Swagger (OpenAPI) annotations
    implementation("io.swagger.core.v3:swagger-jaxrs2:2.2.15")
    implementation("io.swagger:swagger-annotations:1.6.3")

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

openApiGenerate {
    generatorName.set("java")
    inputSpec.set("$rootDir/src/main/resources/openapi.yaml")
    outputDir.set("$buildDir/generated")
    apiPackage.set("com.rhthymiq.controlplaneservice.api")
    modelPackage.set("com.rhthymiq.controlplaneservice.model")
    invokerPackage.set("com.rhthymiq.controlplaneservice.invoker")
    configOptions.set(mapOf(
        "dateLibrary" to "java8",
        "java8" to "true"
    ))
}

sourceSets {
    main {
        java {
            srcDir("$buildDir/generated/src/main/java")
        }
    }
}

tasks.named("compileJava") {
    dependsOn("openApiGenerate")
}