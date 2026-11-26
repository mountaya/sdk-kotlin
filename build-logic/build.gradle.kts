plugins {
    `kotlin-dsl`
    alias(libs.plugins.ktlint)
}

dependencies {
    // The plugins the convention plugins apply. They are dependencies here, never applied here.
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.serialization.plugin)
    implementation(libs.kotlin.compose.plugin)
    implementation(libs.android.kmp.library.plugin)
    implementation(libs.ktlint.gradle.plugin)

    // `registry.yaml` is read at build time only, so no YAML reader reaches a module's classpath.
    implementation(libs.snakeyaml.engine)

    // The gates themselves. They run in a worker process that is handed the compiler's parser,
    // so nothing here ever loads it.
    implementation(project(":gates"))
}

kotlin {
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

testing {
    suites {
        named<JvmTestSuite>("test") {
            useKotlinTest(embeddedKotlinVersion)
        }
    }
}

ktlint {
    version.set(libs.versions.ktlint.asProvider())
    // `kotlin-dsl` generates the accessors the precompiled scripts call, and they are not ours.
    val generated = layout.buildDirectory.get().asFile
    filter {
        exclude { it.file.startsWith(generated) }
    }
}

// One `check` for the whole of build logic, which is what the repository's gate depends on.
tasks.named("check") {
    dependsOn(":gates:check")
}
