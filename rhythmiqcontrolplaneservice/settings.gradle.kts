rootProject.name = "src.main.java.com.rhythmiq.controlplaneservice"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenLocal()
        mavenCentral()
    }
}
include("app")
include("infra")
