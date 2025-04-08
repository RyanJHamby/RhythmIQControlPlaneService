plugins {
    id("java")
    id("idea")
    id("io.freefair.lombok") version "8.6"
    id("application")
}

group = "com.rhythmiq"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://jitpack.io") } // Jitpack for gson-fire
    google()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    testCompileOnly("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")

    implementation("org.apache.logging.log4j:log4j-api:2.22.1")
    implementation("org.apache.logging.log4j:log4j-core:2.22.1")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.22.1")

    implementation("com.fasterxml.jackson.core:jackson-core:2.16.1")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.16.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")

    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("com.google.code.findbugs:jsr305:3.0.2") // Dependency for Nullable annotation

    // JAX-RS API for RESTful services
    implementation("jakarta.ws.rs:jakarta.ws.rs-api:3.0.0")
    implementation("jakarta.inject:jakarta.inject-api:2.0.1")

    // Allows parsing of JSON for invoker client
    implementation("com.github.julman99:gson-fire:1.9.0")

    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.14")

    // AWS Lambda Dependencies
    implementation("com.amazonaws:aws-lambda-java-core:1.2.2")
    implementation("com.amazonaws:aws-lambda-java-events:3.11.4")
    implementation("software.amazon.awssdk:dynamodb:2.20.123") // Use latest AWS SDK version
    implementation("software.amazon.awssdk:core:2.20.123") // AWS core utilities
    implementation("software.amazon.awssdk:auth:2.20.123") // AWS authentication
    implementation("software.amazon.awssdk:regions:2.20.123") // AWS region utilities
    implementation("software.amazon.awssdk:ssm:2.20.109")
    implementation("software.amazon.awssdk:apache-client:2.20.123")
    implementation("com.google.code.gson:gson:2.10.1")

    // Unit Testing
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.5.0")
    testImplementation("org.glassfish.jersey.core:jersey-common:3.1.5")
    testImplementation("org.glassfish.jersey.inject:jersey-hk2:3.1.5")

    implementation("org.glassfish.jersey.containers:jersey-container-grizzly2-http:3.1.3")
    implementation("org.glassfish.jersey.inject:jersey-hk2:3.1.3")
    implementation("org.glassfish.jersey.media:jersey-media-json-jackson:3.1.3")
    implementation("javax.servlet:javax.servlet-api:4.0.1")
    implementation("org.glassfish.jersey.core:jersey-server:3.1.3")
    implementation("org.glassfish.jersey.core:jersey-client:3.1.3")
    implementation("org.glassfish.jersey.core:jersey-common:3.1.3")

    implementation("jakarta.activation:jakarta.activation-api:2.1.1")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.1")
    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.4")

    implementation("com.google.dagger:dagger:2.50")
    annotationProcessor("com.google.dagger:dagger-compiler:2.50")

    implementation("jakarta.servlet:jakarta.servlet-api:6.0.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.clean {
    delete("build")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

sourceSets {
    main {
        java {
            // Remove generated source directory
        }
    }
}

tasks.named("compileJava") {
    // Remove openApiGenerate dependency
}

tasks.named<Jar>("jar") {
    from(sourceSets.main.get().output)
}

tasks.clean {
    // Delete the cdktf.out directory in the infra folder
    delete(file("$rootDir/infra/cdktf.out"))
}

application {
    mainClass.set("com.rhythmiq.controlplaneservice.Main")
    applicationDefaultJvmArgs = listOf("-DSPOTIFY_REDIRECT_URI=http://localhost:3000/api/spotify/callback")
}
