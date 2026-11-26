import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Trail, the Android app of the contract's `sdk/trail.yaml`: a map, a search box over it and a bottom sheet with the
// itinerary's elevation profile, its tables and a navigation along it. It depends on the SDK's modules by their
// coordinates, as an app declares them, and on nothing else the SDK does not already bring but the activity it is
// set into and the Compose foundation it lays its own screen out with.

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
    // The session server's plugin, put on this build's classpath once for the module beside the app that applies it,
    // so the app and the server compile with one Kotlin.
    alias(libs.plugins.kotlin.jvm) apply false
}

val sdkVersion = libs.versions.mountaya.get()

// Trail's settings, as `sdk/trail.yaml` names them: each read from the environment, else from `.env` beside this file,
// which is never committed, and empty where neither sets it. Each becomes a string resource the app reads once at
// runtime with `getString`, which R8 cannot fold, so the release build the size gate measures keeps every screen an
// app with a key shows without a placeholder compiled in.
val dotEnv: Map<String, String> =
    file(".env")
        .takeIf { it.isFile }
        ?.readLines()
        .orEmpty()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") && "=" in it }
        .associate { line ->
            line
                .substringBefore('=')
                .trim()
                .removePrefix("export ")
                .trim() to
                line
                    .substringAfter('=')
                    .trim()
                    .removeSurrounding("\"")
                    .removeSurrounding("'")
        }

fun setting(name: String): String =
    providers.environmentVariable(name).orNull?.takeIf { it.isNotBlank() } ?: dotEnv[name].orEmpty()

val settings =
    mapOf(
        "mountaya_publishable_key" to setting("MOUNTAYA_PUBLISHABLE_KEY"),
        "mountaya_itinerary_id" to setting("MOUNTAYA_ITINERARY_ID"),
        "mountaya_session_url" to setting("MOUNTAYA_SESSION_URL"),
    )

android {
    namespace = "com.example.trail"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()
    buildToolsVersion =
        libs.versions.android.buildTools
            .get()
    defaultConfig {
        applicationId = "com.example.trail"
        // The modules' own floor, so the release build proves every library they carry runs there.
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        versionCode = 1
        versionName = sdkVersion
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        for ((name, value) in settings) resValue("string", name, value)
    }
    buildFeatures {
        resValues = true
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
    compileOptions {
        val target =
            JavaVersion.toVersion(
                libs.versions.jvm.target
                    .get(),
            )
        sourceCompatibility = target
        targetCompatibility = target
    }
}

kotlin {
    compilerOptions {
        allWarningsAsErrors.set(true)
        jvmTarget.set(
            JvmTarget.fromTarget(
                libs.versions.jvm.target
                    .get(),
            ),
        )
    }
}

dependencies {
    implementation("com.mountaya:mountaya-client:$sdkVersion")
    implementation("com.mountaya:mountaya-geo:$sdkVersion")
    implementation("com.mountaya:mountaya-elements:$sdkVersion")
    implementation("com.mountaya:mountaya-navigation:$sdkVersion")
    // The activity the app is set into, and the location access it asks for through the Activity Result API.
    implementation(libs.androidx.activity.compose)
    // The app's own layout around the map: the elements carry Compose's foundation for their own controls, and hand
    // none of it on.
    implementation(libs.androidx.compose.foundation)

    // The journey of `sdk/trail.yaml`, a Compose UI test run under AndroidX's instrumentation runner.
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.runner)
    // The Espresso a composable's test waits for idle through, newer than the one Compose's test names, which fails on
    // API 37 (the version catalog says why).
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(kotlin("test"))
    // The activity a test's composable is mounted in, merged into the debug app the journey installs.
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

ktlint {
    version.set(libs.versions.ktlint.asProvider())
}

// The repository's app-size gate reads what this build carries (`size.gradle.kts`). An app of your own leaves it out.
apply(from = "size.gradle.kts")
