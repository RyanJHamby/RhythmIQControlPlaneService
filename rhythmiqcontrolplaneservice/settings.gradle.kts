rootProject.name = "src.main.java.com.rhthymiq.controlplaneservice"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenLocal()
        mavenCentral()
    }
}

include(":service")
include(":internal-model")