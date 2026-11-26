pluginManagement {
    includeBuild("build-logic")
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
}

rootProject.name = "sdk-kotlin"

// A module is listed here once it has content, so nothing empty is ever built or published.
// `mountaya-admin` is the client's admin unit, beside its core: an app that draws a map, edits an itinerary,
// queries the Data API or navigates links the core alone. `mountaya-elements` builds on every target: its map
// engine and the `map` rules are common code, and its renderer, the Compose map over MapLibre, is its android
// target's alone.
include(":mountaya-client")
include(":mountaya-admin")
include(":mountaya-geo")
include(":mountaya-elements")
include(":conformance")

// An Android module, built where the android target is: `-Pmountaya.targets` reads as the modules
// read it, so a run that leaves android out needs no Android SDK and configures no Android module.
val targets =
    providers
        .gradleProperty("mountaya.targets")
        .map { value ->
            value
                .split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toSet()
        }.getOrElse(setOf("jvm", "android", "ios"))
if ("android" in targets) include(":mountaya-navigation")

// The outside-in consumer (SPEC.md section 7), a build of its own that resolves the modules by
// their coordinates through this composite. It is included where this build is the one run: the
// consumer run on its own includes this build instead.
if (gradle.parent == null) includeBuild("consumer")

// Trail (SPEC.md section 7), the sample app of the contract's `sdk/trail.yaml`, Android's: a build of its own that
// resolves the modules by their coordinates the same way, included where this build is the one run and android is
// built, so the gate builds it, measures it and runs its journey. Trail run on its own includes this build instead.
if (gradle.parent == null && "android" in targets) includeBuild("examples/trail")
