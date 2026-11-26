// The outside-in consumer of SPEC.md section 7, a build of its own. It depends on the modules by
// their coordinates, as an app declares them, and Gradle's composite build resolves those
// coordinates to the modules beside it: nothing is published, and nothing is read from `~/.m2`.

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
        // The repository's one catalog, so the consumer compiles with the Kotlin and the Android
        // plugin the modules are built with, and depends on them at their own version.
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "consumer"

// The repository's build, whose modules the coordinates resolve to.
includeBuild("..")
