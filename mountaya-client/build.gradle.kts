import com.mountaya.build.generator.GenerateClientVersion

plugins {
    id("mountaya.multiplatform-library")
    id("mountaya.published-module")
    id("mountaya.conformance-runners")
    id("mountaya.generated-client")
    id("org.jetbrains.kotlin.plugin.serialization")
}

description =
    "Mountaya's client for the Map Studio API, the Tile API and the Data API, holding the transport's rules."

// The version every request names this SDK by, `X-Mountaya-Client: mountaya-kotlin/<version>` (SPEC.md
// section 4): printed at build time from the catalog's `mountaya`, the one place it is written, into a
// source directory of the build's own that common code compiles with.
val generateClientVersion =
    tasks.register<GenerateClientVersion>("generateClientVersion") {
        group = "build"
        description = "Prints the version every request names this SDK by, from the version catalog's `mountaya`."
        sdkVersion.set(libs.versions.mountaya)
        packageName.set("com.mountaya.client")
        output.set(layout.buildDirectory.dir("generated/mountaya/clientVersion"))
    }

// The test that holds the printed version to the catalog reads the catalog itself, a declared input.
val catalogFile = rootProject.layout.projectDirectory.file("gradle/libs.versions.toml")
tasks.matching { it.name == "jvmTest" }.withType<Test>().configureEach {
    systemProperty("mountaya.catalog", catalogFile.asFile.absolutePath)
    inputs.file(catalogFile).withPathSensitivity(PathSensitivity.RELATIVE).withPropertyName("catalog")
}

kotlin {
    // The module declares `InternalMountayaApi`, the marker of what Mountaya's modules read across their boundary,
    // the seam the admin unit's printed methods hand their calls over through among it (SPEC.md section 7), and
    // reads its own declarations throughout: every other module opts in where it reads one.
    compilerOptions {
        optIn.add("com.mountaya.client.InternalMountayaApi")
    }

    sourceSets {
        commonMain.configure { kotlin.srcDir(generateClientVersion) }
        commonMain.dependencies {
            // Both reach a consumer's own signatures: an event stream is a `Flow`, and a value
            // the contract leaves untyped is a `JsonElement`.
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.serialization.json)
            // The platform's exchange sends through Ktor's client, and no Ktor type reaches a
            // public signature, so it stays off a consumer's compile classpath.
            implementation(libs.ktor.client.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
            // The sockets of the local server the platform's exchange is tested against.
            implementation(libs.ktor.network)
        }
        // Each engine is named in one `actual`: OkHttp on the jvm and android, Darwin on iOS. A
        // target the switch leaves out has no source set to configure.
        matching { it.name == "jvmMain" || it.name == "androidMain" }.configureEach {
            dependencies {
                implementation(libs.ktor.client.okhttp)
                implementation(libs.okhttp)
            }
        }
        matching { it.name == "iosMain" }.configureEach {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
    }
}

conformanceRunners {
    // The root build's table gives this module the `api: false` transport entries, which a
    // consumer only observes. They run here, the one place Kotlin shows `internal` to a test;
    // the ones a consumer calls run from the conformance module.
    runnerPackage.set("com.mountaya.client")
    runnerDirectory.set("com/mountaya/client/conformance/rules")
}
