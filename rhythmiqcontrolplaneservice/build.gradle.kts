plugins {
    id("java")
    id("idea")
    id("org.openapi.generator") version "7.12.0"
    id("io.freefair.lombok") version "8.6"
}

group = "com.rhythmiq.controlplaneservice"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://jitpack.io") } // Jitpack for gson-fire
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    testCompileOnly("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")

    implementation("org.apache.logging.log4j:log4j-api:2.22.1")
    implementation("org.apache.logging.log4j:log4j-core:2.22.1")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.22.1") // Bridges SLF4J to Log4j2

    implementation("com.fasterxml.jackson.core:jackson-core:2.16.1")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.16.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")

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
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("ch.qos.logback:logback-classic:1.2.11")

    // Unit Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testImplementation("org.mockito:mockito-core:5.5.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register<Zip>("packageLambda") {
    dependsOn("jar")

    val lambdaDir = layout.buildDirectory.dir("lambda")

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from({ zipTree(tasks.getByName<Jar>("jar").archiveFile) }) // Include the JAR
    from(configurations.runtimeClasspath.get().files.map { if (it.isDirectory) it else zipTree(it) }) // Include dependencies

    destinationDirectory.set(lambdaDir)
    archiveFileName.set("lambda.zip")
}

tasks.register("buildInfra") {
    group = "infrastructure"
    description = "Install dependencies, compile TypeScript, and synthesize Terraform CDK"
    dependsOn("packageLambda")

    doLast {
        exec {
            commandLine("npm", "install")
            workingDir = file("$rootDir/infra")
            isIgnoreExitValue = false  // Fail if npm install fails
        }
        exec {
            commandLine("npx", "tsc")
            workingDir = file("$rootDir/infra")
            isIgnoreExitValue = false
        }
        exec {
            commandLine("npx", "cdktf", "synth")
            workingDir = file("$rootDir/infra")
            isIgnoreExitValue = false
        }
    }
}

tasks.named("build") {
    dependsOn("buildInfra")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

openApiGenerate {
    generatorName.set("java")
    inputSpec.set("$rootDir/src/main/resources/openapi.yaml")
    outputDir.set(layout.buildDirectory.dir("generated").get().asFile.absolutePath)
    apiPackage.set("com.rhythmiq.controlplaneservice.api")
    modelPackage.set("com.rhythmiq.controlplaneservice.model")
    invokerPackage.set("com.rhythmiq.controlplaneservice.invoker")
    configOptions.set(mapOf(
        "dateLibrary" to "java8"
    ))
}

tasks.register("deployInfra") {
    group = "infrastructure"
    description = "Automates Terraform synthesis and deployment for all stacks in infra/"

    doLast {
        val infraDir = file("infra")

        // Step 1: Run synthesis and wait for completion
        println("🔄 Synthesizing Terraform stacks...")
        val synthOutput = "npx cdktf synth".runCommand(infraDir)
        if (!synthOutput.contains("Synthesis complete")) {
            throw GradleException("❌ CDKTF synthesis failed. Check logs.")
        }

        // Step 2: Get list of stacks and filter out unwanted lines
        println("🔍 Retrieving list of stacks...")
        val stacks = "cdktf list".runCommand(infraDir)
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.contains("Starting") } // Remove bad entries

        if (stacks.isEmpty()) {
            throw GradleException("❌ No valid Terraform stacks found in infra/.")
        }

        // Step 3: Deploy each stack sequentially
        stacks.forEach { stack ->
            println("🚀 Deploying stack: $stack")
            val deployOutput = "cdktf deploy $stack --auto-approve".runCommand(infraDir)

            if ("Apply complete!" in deployOutput || "No changes. Your infrastructure matches the configuration." in deployOutput) {
                println("✅ Stack '$stack' deployed successfully!")
            } else {
                throw GradleException("❌ Deployment failed for stack '$stack'. Check logs.")
            }
        }
    }
}

// Helper function to run shell commands and return output
fun String.runCommand(workingDir: File): String {
    val process = ProcessBuilder("sh", "-c", this)
        .directory(workingDir)
        .redirectErrorStream(true)
        .start()

    return process.inputStream.bufferedReader().use { it.readText() }.trim()
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

tasks.named<Jar>("jar") {
    from(sourceSets.main.get().output)
}

tasks.clean {
    // Delete the cdktf.out directory in the infra folder
    delete(file("$rootDir/infra/cdktf.out"))
}
