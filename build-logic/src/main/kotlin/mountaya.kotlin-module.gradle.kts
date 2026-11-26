import com.mountaya.build.classpath.CheckRuntimeClasspathTask
import com.mountaya.build.contract.CoverageGate
import com.mountaya.build.device.DeviceTestResults
import com.mountaya.build.gates.GateTask
import com.mountaya.build.gates.SourceTree
import com.mountaya.build.seams.BuildClock
import com.mountaya.build.targets.MountayaTargets
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

// What every Kotlin module of this SDK holds, whatever targets it builds: explicit API mode, warnings
// as errors, the language and API level of the floor the version catalog names, the formatter, and
// its sources handed to the repository's gates. A convention plugin over this one declares the
// targets: `mountaya.multiplatform-library` the jvm, android and iOS ones, `mountaya.android-library`
// android's alone.

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jlleitschuh.gradle.ktlint")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun pinned(name: String): String = catalog.findVersion(name).get().requiredVersion

val enabled = MountayaTargets.of(project)
val moduleName = project.name

/** The task that compiles the android target's device tests, which no run on the host executes. */
val deviceTestCompile = "compileAndroidDeviceTest"

/** The task that packages them into the APK a device installs, past the checks each AAR it carries asks for. */
val deviceTestPackage = "packageAndroidDeviceTest"

kotlin {
    explicitApi()
    coreLibrariesVersion = pinned("kotlin-floor-libraries")

    compilerOptions {
        allWarningsAsErrors.set(true)
        languageVersion.set(KotlinVersion.fromVersion(pinned("kotlin-floor")))
        apiVersion.set(KotlinVersion.fromVersion(pinned("kotlin-floor")))
    }

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

ktlint {
    version.set(pinned("ktlint"))
    val generated = layout.buildDirectory.get().asFile
    filter {
        // Generated sources are printed, never formatted: those the build writes, and the generated
        // package `generateClient` prints into the source tree. Its bytes are what the model prints,
        // and `checkGeneratedClient` holds them there, so a formatter's opinion would be a second
        // author of the same file.
        exclude { it.file.startsWith(generated) }
        exclude { "generated" in it.relativePath.segments }
    }
}

// The module's source trees, handed to the repository's gates. A source set is a directory
// under src/, which is the convention the Kotlin plugin itself names them by, so a source set
// a target switch leaves out of this run is still scanned.
val sourceRoot = layout.projectDirectory.dir("src")
val moduleTrees =
    provider {
        sourceRoot.asFile
            .listFiles()
            .orEmpty()
            .filter { File(it, "kotlin").isDirectory }
            .sortedBy { it.name }
            .map {
                SourceTree(
                    moduleName,
                    it.name,
                    it.name.endsWith("Test") || it.name == "test",
                    File(it, "kotlin"),
                ).encode()
            }
    }

rootProject.tasks.withType<GateTask>().configureEach {
    trees.addAll(moduleTrees)
    sources.from(sourceRoot.asFileTree.matching { include("*/kotlin/**/*.kt") })
}

// The built-artifact walk, over this module's resolved classpath for every target built.
val checkRuntimeClasspath =
    tasks.register<CheckRuntimeClasspathTask>("checkRuntimeClasspath") {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Fails on a path from this module to a dependency SPEC section 7 says it never reaches."
        module.set(moduleName)
        val walked = Regex("(RuntimeClasspath|CompileKlibraries)$")
        configurations
            .matching { it.isCanBeResolved && walked.containsMatchIn(it.name) && !it.name.contains("Test") }
            .all { roots.put(name, incoming.resolutionResult.rootComponent) }
    }

tasks.named("check") { dependsOn(checkRuntimeClasspath) }

// `check` compiles a target only to test it, and some targets are never tested here: iosArm64
// runs on no simulator, and the android target's device tests run on a device. `assemble` builds
// every enabled target's artifact, so the gate also proves the module compiles for each of them, and
// the device tests are compiled and packaged with it, so an APK an emulator would install is never
// left unbuilt, and what an AAR it carries asks of it, a platform floor or a build setting, fails the
// gate rather than the device run.
rootProject.tasks.named("verify") {
    dependsOn(tasks.named("assemble"), tasks.named("check"), tasks.named("ktlintCheck"))
    dependsOn(tasks.matching { it.name == deviceTestCompile || it.name == deviceTestPackage })
}
rootProject.tasks.named(CoverageGate.NAME) {
    mustRunAfter(tasks.named("assemble"), tasks.named("check"), tasks.named("ktlintCheck"))
}
if (enabled.android) {
    // AGP's test engine logs a test APK it could not install, or a device that stopped answering, and
    // passes the run all the same, so each run is held to the report it wrote: one that names no test
    // fails (`DeviceTestResults`). The run's start is read off the build's one clock seam, `BuildClock`.
    val deviceReports = layout.buildDirectory.dir("outputs/androidTest-results/connected")
    val deviceRuns = tasks.matching { it.name == "connectedAndroidDeviceTest" }
    deviceRuns.configureEach {
        var started = 0L
        doFirst { started = BuildClock.nowMillis() }
        doLast {
            if (DeviceTestResults.testsRun(deviceReports.get().asFile, started) == 0) {
                throw GradleException(
                    "$path ran no test: its APK did not install, or the device stopped answering, as the log above says.",
                )
            }
        }
    }
    rootProject.tasks.named("verifyDevice") {
        dependsOn(deviceRuns)
    }
}
