// Deliverance Icons: a standalone Gradle build (own folder, own licence), separate from the launcher build.
// Build with the repo's wrapper:  ./gradlew -p iconpack :pack:assembleHeaterDebug  (see iconpack/README.md)
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "9.4.1"
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "deliverance-icons"
include(":pack")
