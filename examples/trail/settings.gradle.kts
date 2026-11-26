// Trail, the sample app the contract's `sdk/trail.yaml` describes, a build of its own: the Android app at this
// directory, and the session server beside it that mints its session tokens. It depends on the SDK's modules by
// their coordinates, as an app declares them, and resolves them to the checkout two levels up through this
// composite: nothing is published, and nothing is read from `~/.m2`. An app of your own drops the
// `includeBuild` below and resolves the same coordinates from Maven Central.

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        // The repository's one catalog, so Trail builds with the Kotlin and the Android plugin the modules are
        // built with, and depends on them at their own version.
        create("libs") {
            from(files("../../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "trail"

// The session server: a JVM program that mints the app's session tokens with the secret key, which never
// reaches the device (`mise run trail:session`).
include(":session")

// The SDK whose modules the coordinates resolve to.
includeBuild("../..")
