import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// A Kotlin Multiplatform module as an app's shared code is one: it depends on the modules by
// their coordinates and compiles against their public surface alone, warnings as errors, on every
// target the repository's gate builds. A public type with an internal constructor, an interface
// whose member names an internal type, or a variant a target cannot resolve fails here, where no
// test inside a module can see it. Its `readme` package holds the README's Kotlin snippets,
// printed from it (SPEC.md section 10).

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library) apply false
    // The map's composable is called from the Android code, as an app's own composables call it. The
    // compiler is the jvm and iOS targets' too, where it compiles no composable.
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
}

// The repository's targets switch, read as its build reads it, so a run that leaves a target out
// builds the consumer without it too.
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
val android = "android" in targets
val ios = "ios" in targets

if (android) apply(plugin = "com.android.kotlin.multiplatform.library")

val sdkVersion = libs.versions.mountaya.get()

// The Kotlin skill's snippets (SPEC.md section 10), which the repository's build reads from the `ai` checkout
// beside it and prints here before this build compiles them, never committed: each source set's are the
// fences whose imports name that target, and a checkout that is not there prints none.
val skillSnippets = layout.buildDirectory.dir("generated/skill")
val printSkillSnippets =
    gradle.includedBuilds
        .single { it.projectDir == rootDir.parentFile }
        .task(":checkSkillSnippets")

// The Kotlin fences of the Markdown documents `mise run snippets -- <file.md> [...]` names (SPEC.md section 10), which
// the repository's build prints here, never committed, a file a fence in the source set its imports name. They are
// sources only where `-Pmountaya.snippets` names documents, so the gate's consumer build never reads them. An app's
// fences are set into an activity and a test's run under `runTest` with kotlin.test's `@Test`, so the activity library,
// the coroutines' test library and kotlin.test over JUnit, as an app's build declares them, join them, then alone.
val docSnippets = layout.buildDirectory.dir("generated/snippets")
val compilesDocSnippets = providers.gradleProperty("mountaya.snippets").isPresent
val printDocSnippets =
    gradle.includedBuilds
        .single { it.projectDir == rootDir.parentFile }
        .task(":printDocSnippets")

composeCompiler {
    targetKotlinPlatforms.set(setOf(org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.androidJvm))
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(
                JvmTarget.fromTarget(
                    libs.versions.jvm.target
                        .get(),
                ),
            )
        }
    }
    if (ios) iosSimulatorArm64()
    if (android) {
        (this as ExtensionAware).extensions.configure(KotlinMultiplatformAndroidLibraryTarget::class.java) {
            namespace = "com.example.consumer"
            compileSdk =
                libs.versions.android.compileSdk
                    .get()
                    .toInt()
            buildToolsVersion =
                libs.versions.android.buildTools
                    .get()
            minSdk =
                libs.versions.android.minSdk
                    .get()
                    .toInt()
            compilerOptions {
                jvmTarget.set(
                    JvmTarget.fromTarget(
                        libs.versions.jvm.target
                            .get(),
                    ),
                )
            }
        }
    }

    compilerOptions {
        allWarningsAsErrors.set(true)
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(skillSnippets.map { it.dir("commonMain/kotlin") })
        }
        jvmMain {
            kotlin.srcDir(skillSnippets.map { it.dir("jvmMain/kotlin") })
        }
        if (android) {
            androidMain {
                kotlin.srcDir(skillSnippets.map { it.dir("androidMain/kotlin") })
            }
        }
        if (compilesDocSnippets) {
            commonMain {
                kotlin.srcDir(docSnippets.map { it.dir("commonMain/kotlin") })
            }
            commonMain.dependencies {
                implementation(libs.kotlinx.coroutines.test)
                implementation(kotlin("test"))
            }
            jvmMain.dependencies {
                implementation(kotlin("test-junit"))
            }
            jvmMain {
                kotlin.srcDir(docSnippets.map { it.dir("jvmMain/kotlin") })
            }
            if (android) {
                androidMain {
                    kotlin.srcDir(docSnippets.map { it.dir("androidMain/kotlin") })
                }
                androidMain.dependencies {
                    implementation(libs.androidx.activity.compose)
                    implementation(kotlin("test-junit"))
                }
            }
        }
        commonMain.dependencies {
            implementation("com.mountaya:mountaya-client:$sdkVersion")
            // The admin unit, which a server or an app that manages its organization declares beside the client.
            implementation("com.mountaya:mountaya-admin:$sdkVersion")
            implementation("com.mountaya:mountaya-geo:$sdkVersion")
            // The map's state and engine are every target's; its composable is Android's.
            implementation("com.mountaya:mountaya-elements:$sdkVersion")
        }
        // The navigation engine is Android's: an app's shared code follows a route on Geo's machine,
        // and its Android side navigates on the device's engine, the same machine with the device's seams.
        if (android) {
            androidMain.dependencies {
                implementation("com.mountaya:mountaya-navigation:$sdkVersion")
                // The app's own layout around the map and its controls, as an app's build declares it: the
                // elements carry Compose's foundation for their own controls, and hand none of it on.
                implementation(libs.androidx.compose.foundation)
            }
        }
    }
}

// Every compilation reads the skill's snippets, so it runs after the repository's build prints them, and the
// documents' fences too where they are named.
tasks.matching { it.name.startsWith("compile") }.configureEach {
    dependsOn(printSkillSnippets)
    if (compilesDocSnippets) dependsOn(printDocSnippets)
}

ktlint {
    version.set(libs.versions.ktlint.asProvider())
    val printed = skillSnippets.get().asFile
    val printedFences = docSnippets.get().asFile
    filter {
        // The README's, the skill's and the documents' snippets are printed from them, never formatted.
        exclude { "readme" in it.relativePath.segments && it.file.name == "ReadmeSnippets.kt" }
        exclude { it.file.startsWith(printed) }
        exclude { it.file.startsWith(printedFences) }
    }
}
