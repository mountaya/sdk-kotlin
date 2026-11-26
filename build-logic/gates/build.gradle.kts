plugins {
    // The Kotlin plugin the parent project's `kotlin-dsl` already put on the classpath.
    kotlin("jvm")
    alias(libs.plugins.ktlint)
}

dependencies {
    // The compiler's own parser. Compiled against, never bundled: the build hands it to the
    // worker process a gate runs in.
    compileOnly(libs.kotlin.compiler.embeddable)
    testImplementation(libs.kotlin.compiler.embeddable)
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
}
