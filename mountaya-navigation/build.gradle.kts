plugins {
    id("mountaya.android-library")
    id("mountaya.published-module")
}

description =
    "How an Android app follows a route: Mountaya's navigation engine on the device, which says each maneuver aloud."

// The navigation engine an Android app follows a route with, Android alone, since its seams read the
// platform's location services and speech (SPEC.md sections 1 and 2). It is Geo's machine with its
// guidance, written here, and stands on Geo's public API alone: it links no navigation library
// (SPEC.md section 7).

kotlin {
    sourceSets {
        androidMain.dependencies {
            // Its engine is a `NavigationEngine` and its seams speak Geo's vocabulary, which sit in its
            // own public signatures.
            api(project(":mountaya-geo"))
        }
        commonTest.dependencies {
            // The engines' tests record every state, on a test's virtual time.
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
