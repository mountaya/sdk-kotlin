plugins {
    id("mountaya.multiplatform-library")
    id("mountaya.published-module")
    id("mountaya.conformance-runners")
    id("mountaya.generated-elements")
}

description =
    "What Mountaya decides about its data: units, values, the profile, the camera, the layers, navigation."

// The geo layer: the contract's rules, as pure functions over data. It calls nothing and renders
// nothing, and holds every rule in common code, so each target runs the same rule (SPEC.md
// sections 2, 6 and 7).

kotlin {
    sourceSets {
        commonMain.dependencies {
            // For the wire vocabulary alone: the generated types Geo aliases and the GeoJSON types
            // sit in Geo's own public signatures. That Geo never calls the client is held by the
            // boundary gate, since Kotlin has no type-only dependency.
            api(project(":mountaya-client"))
            // An engine reports its state as a `StateFlow`, which sits in Geo's own public
            // signatures; the client carries the same library, so this names it rather than adds it.
            api(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            // The engines' tests record every state a `StateFlow` reports, on a test's virtual time.
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

conformanceRunners {
    // The root build's table gives this module the `api: false` geo entries, which a consumer
    // only observes. They run here, the one place Kotlin shows `internal` to a test; the ones a
    // consumer calls run from the conformance module.
    runnerPackage.set("com.mountaya.geo")
    runnerDirectory.set("com/mountaya/geo/conformance/rules")
}
