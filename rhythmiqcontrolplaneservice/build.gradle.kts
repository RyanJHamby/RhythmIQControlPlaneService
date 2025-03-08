plugins {
    id("java")
    id("idea")
    id("org.openapi.generator") version "7.12.0"
}

group = "com.rhthymiq.controlplaneservice"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://jitpack.io") } // Jitpack for gson-fire
}

dependencies {
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("com.google.code.findbugs:jsr305:3.0.2") // Dependency for Nullable annotation

    // JAX-RS API for RESTful services
    implementation("jakarta.ws.rs:jakarta.ws.rs-api:3.0.0")

    implementation("com.google.dagger:dagger:2.50")

    // Allows parsing of JSON for invoker client
    implementation("com.github.julman99:gson-fire:1.9.0")

    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.14")

    implementation("io.swagger.core.v3:swagger-jaxrs2:2.2.15")

    // AWS Lambda Dependencies
    implementation("com.amazonaws:aws-lambda-java-core:1.2.3")
    implementation("com.amazonaws:aws-lambda-java-events:3.11.0")
    implementation("software.amazon.awssdk:dynamodb:2.20.123") // Use latest AWS SDK version
    implementation("software.amazon.awssdk:core:2.20.123") // AWS core utilities
    implementation("software.amazon.awssdk:auth:2.20.123") // AWS authentication
    implementation("software.amazon.awssdk:regions:2.20.123") // AWS region utilities

    // Logging
    implementation("org.slf4j:slf4j-simple:2.0.9")

    // Unit Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testImplementation("org.mockito:mockito-core:5.5.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register("buildInfra") {
    group = "infrastructure"
    description = "Install dependencies and build Terraform CDK project"

    doLast {
        exec {
            commandLine("npm", "install")
            workingDir = file("$rootDir/infra")
        }
        exec {
            commandLine("npx", "cdktf", "synth")
            workingDir = file("$rootDir/infra")
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

openApiGenerate {
    generatorName.set("java")
    inputSpec.set("$rootDir/src/main/resources/openapi.yaml")
    outputDir.set(layout.buildDirectory.dir("generated").get().asFile.absolutePath)
    apiPackage.set("com.rhthymiq.controlplaneservice.api")
    modelPackage.set("com.rhthymiq.controlplaneservice.model")
    invokerPackage.set("com.rhthymiq.controlplaneservice.invoker")
    configOptions.set(mapOf(
        "dateLibrary" to "java8"
    ))
}

sourceSets {
    main {
        java {
            srcDir(layout.buildDirectory.dir("generated/src/main/java"))
        }
    }
}

tasks.named("compileJava") {
    dependsOn("openApiGenerate")
}

tasks.clean {
    // Delete the cdktf.out directory in the infra folder
    delete(file("$rootDir/infra/cdktf.out"))
}