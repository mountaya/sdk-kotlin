import com.mountaya.build.targets.AndroidTarget
import com.mountaya.build.targets.MountayaTargets
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// A Kotlin Multiplatform module of this SDK: jvm, android and the two iOS targets, over what every
// module of it holds (`mountaya.kotlin-module`).

plugins {
    id("mountaya.kotlin-module")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun pinned(name: String): String = catalog.findVersion(name).get().requiredVersion

val enabled = MountayaTargets.of(project)

// The android target's plugin is applied only where the target is built, so a jvm-only run
// needs no Android SDK at all.
if (enabled.android) apply(plugin = "com.android.kotlin.multiplatform.library")

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(pinned("jvm-target")))
            // The build runs on a newer JDK than the floor. This holds the API it may call to the floor's.
            freeCompilerArgs.add("-Xjdk-release=${pinned("jvm-target")}")
            // An interface's default members are the JVM's own default methods and nothing else: no
            // release before this one compiled them into `DefaultImpls`, so none is kept for one.
            jvmDefault.set(JvmDefaultMode.NO_COMPATIBILITY)
        }
    }

    if (enabled.ios) {
        // A klib records the path of every source file it is compiled from, for its debug
        // information. Each is recorded relative to the repository root, so a published klib names
        // no directory of the machine that built it, and every host builds the same bytes.
        val relativePaths = "-Xklib-relative-path-base=${rootDir.absolutePath}"
        iosArm64 { compilerOptions { freeCompilerArgs.add(relativePaths) } }
        iosSimulatorArm64 { compilerOptions { freeCompilerArgs.add(relativePaths) } }
    }

    // Its common tests run on the jvm, so the android target runs device tests alone.
    if (enabled.android) AndroidTarget.configure(project, this, hostTests = false)
}
