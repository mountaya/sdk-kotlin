package com.mountaya.build.targets

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.ExtensionAware
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * AndroidTarget is the android target every module of this SDK declares the same way: its namespace, the platform it
 * compiles against and the lowest it runs on, from the version catalog, and its device tests.
 *
 * Every module's device tests run under AndroidX's instrumentation runner, on an emulator
 * (`mise run test:device`), with common tests in the same tree. A module whose own tests run on the
 * host JVM against the android target, as `mountaya-navigation`'s do wherever they read no platform
 * service, asks for [hostTests] too; a multiplatform module's common tests run on its jvm target
 * instead, so it asks for none, and `mountaya-elements` declares host tests of a tree of their own in
 * its build file, for its request hook alone.
 */
object AndroidTarget {
    /** The runner a device test runs under, the one `androidx.test:runner` ships. */
    const val DEVICE_TEST_RUNNER = "androidx.test.runner.AndroidJUnitRunner"

    /** Configures [kotlin]'s android target for [project], with host tests where [hostTests] says. */
    fun configure(
        project: Project,
        kotlin: KotlinMultiplatformExtension,
        hostTests: Boolean,
    ) {
        val catalog = project.extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

        fun pinned(name: String): String = catalog.findVersion(name).get().requiredVersion

        val moduleName = project.name
        (kotlin as ExtensionAware).extensions.configure(KotlinMultiplatformAndroidLibraryTarget::class.java) {
            namespace = "com.mountaya.${moduleName.removePrefix("mountaya-").replace("-", "")}"
            compileSdk = pinned("android-compileSdk").toInt()
            buildToolsVersion = pinned("android-buildTools")
            minSdk = pinned("android-minSdk").toInt()
            withDeviceTestBuilder { sourceSetTreeName = "test" }.configure {
                instrumentationRunner = DEVICE_TEST_RUNNER
            }
            if (hostTests) withHostTestBuilder { sourceSetTreeName = "test" }
            compilerOptions {
                jvmTarget.set(JvmTarget.fromTarget(pinned("jvm-target")))
                jvmDefault.set(JvmDefaultMode.NO_COMPATIBILITY)
            }
        }
        kotlin.sourceSets.matching { it.name == "androidDeviceTest" }.configureEach {
            dependencies { implementation(catalog.findLibrary("androidx-test-runner").get()) }
        }
    }
}
