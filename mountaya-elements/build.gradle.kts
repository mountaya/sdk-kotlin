import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    id("mountaya.multiplatform-library")
    id("mountaya.published-module")
    id("mountaya.conformance-runners")
    id("org.jetbrains.kotlin.plugin.compose")
}

description =
    "How an app shows Mountaya's data: the map's state and engine on every target, drawn on Android by a Compose map over MapLibre."

// The elements binding (SPEC.md sections 1, 2 and 8). Its map engine, the state it holds and the
// `map` layer's rules name no renderer type and are common code, so they build and run on every
// target and a consumer who draws a map of their own reads the same state; the renderer, a
// composable over MapLibre Native's map and the hook MapLibre's own requests leave through, is the
// android target's alone. It stands on Geo's public API and the client's, and opens no connection of
// its own: the map's requests are MapLibre's, given the client's credential by the hook.

// Composables are Android's alone here: the jvm and iOS targets compile no composable, and carry no
// Compose runtime to compile one against.
composeCompiler {
    targetKotlinPlatforms.set(setOf(KotlinPlatformType.androidJvm))
}

// The request hook signs a request without loading MapLibre, so its tests run on the host JVM against
// the android target, in `androidHostTest`, a tree of its own: the module's common tests run on the jvm
// target already, and the renderer's other tests load MapLibre, on a device (SPEC.md section 9).
plugins.withId("com.android.kotlin.multiplatform.library") {
    kotlin {
        (this as ExtensionAware).extensions.configure(KotlinMultiplatformAndroidLibraryTarget::class.java) {
            withHostTestBuilder { sourceSetTreeName = "unitTest" }
        }
    }
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // The map's state names Geo's values and the client's wire types, and its engine takes the
            // app's `Mountaya`: every one sits in this module's own public signatures.
            api(project(":mountaya-geo"))
        }
        commonTest.dependencies {
            // The engine's tests read its `StateFlow`, and drive the client on a scripted exchange.
            implementation(libs.kotlinx.coroutines.test)
        }
        // A target the switch leaves out has no source set to configure.
        matching { it.name == "androidMain" }.configureEach {
            dependencies {
                // `MapView`'s ready callback hands back MapLibre's own map, and its `okHttpClient` is the
                // app's own client for MapLibre's requests: both types cross, so both are `api`.
                api(libs.maplibre.android)
                api(libs.okhttp)
                // `MapView` is a composable that takes the host's `Modifier`.
                api(libs.androidx.compose.ui)
                // The controls and panels are laid out, pressed and written with Compose's foundation, which no
                // public signature names, so an app reaches none of it through this module.
                implementation(libs.androidx.compose.foundation)
                // The lifecycle MapLibre's map is driven by, read from the composition.
                implementation(libs.androidx.lifecycle.runtime.compose)
                // The window's own insets, which the map's safe area is read off, through `ViewCompat`.
                implementation(libs.androidx.core)
            }
        }
        matching { it.name == "androidHostTest" }.configureEach {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        matching { it.name == "androidDeviceTest" }.configureEach {
            dependencies {
                implementation(libs.androidx.compose.ui.test.junit4)
                implementation(libs.androidx.compose.ui.test.manifest)
                // The Espresso the composable's tests wait for idle through, newer than the one
                // Compose's test names, which fails on API 37 (the version catalog says why).
                implementation(libs.androidx.test.espresso.core)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}

conformanceRunners {
    // The root build's table gives this module the `api: false` map entries, which only the engines
    // observe. They run here, the one place Kotlin shows `internal` to a test, on every target; the
    // one a consumer calls, `map.engine.patch`, runs from the conformance module.
    runnerPackage.set("com.mountaya.elements")
    runnerDirectory.set("com/mountaya/elements/conformance/rules")
}
