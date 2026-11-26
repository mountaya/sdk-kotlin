dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    versionCatalogs {
        // One catalog for the build and for the logic that configures it, so a version moves once.
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"

// The source gates are their own project because they compile against the compiler's parser,
// which must never be on the classpath the build's own Kotlin plugin is loaded from.
include(":gates")
