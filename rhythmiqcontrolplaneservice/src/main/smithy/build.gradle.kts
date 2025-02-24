plugins {
    `java-library`
    id("software.amazon.smithy.gradle.smithy-jar").version("1.2.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}

smithy {
    tags.addAll("Foo", "com.baz:bar")
}