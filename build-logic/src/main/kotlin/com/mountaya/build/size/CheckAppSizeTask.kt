package com.mountaya.build.size

import com.mountaya.build.contract.ContractCheckout
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

/**
 * CheckAppSizeTask is the app-size gate of SPEC.md section 7, run as `mise run size:check` and in `mise run ci` after
 * `checkConsumer`: the release build of Trail, `examples/trail`, the sample app of the contract's `sdk/trail.yaml`,
 * read package by package with `apkanalyzer dex packages` through R8's mapping, and each share of `AppSize` held to the
 * baseline beside the app (`SizeBaseline`), each library the SDK brings at the version the app's release runtime
 * classpath resolved, which the app's build writes with the graph of that classpath, where a library an SDK module
 * brings that no share names fails it.
 *
 * It reads dex code alone: the native libraries an app carries, MapLibre Native's core above all, are compiled whole
 * whatever an app calls, and move with releases a consumer's own ranges choose, while the dex code is where R8 shrinks.
 * Where [write] is set, as `MOUNTAYA_SIZE_BASELINE=write` sets it, it records the figures in place of comparing them.
 * It declares no output, so it runs on every gate.
 */
abstract class CheckAppSizeTask : DefaultTask() {
    /** The app's release APK, which R8 shrank. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val apk: RegularFileProperty

    /** The mapping R8 wrote beside it, which reads the shrunk names back. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val mapping: RegularFileProperty

    /**
     * The version of each module the app's release build carries, one `group:name:version` a line, which the app's build
     * writes from its release runtime classpath's resolution result (`ResolvedVersions`).
     */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val versions: RegularFileProperty

    /**
     * The graph of the app's release runtime classpath, one edge a line, which the app's build writes beside the versions
     * (`ReleaseGraph`).
     */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val graph: RegularFileProperty

    /**
     * The version of `sdk/trail.yaml` the app follows, read from the contract checkout, or unset where the checkout is
     * absent, which a run records nothing in and compares nothing in, and which fails under CI.
     */
    @get:Input
    @get:Optional
    abstract val scenario: Property<Int>

    /** Whether an absent contract checkout fails the run, as it does under CI. */
    @get:Input
    abstract val ci: Property<Boolean>

    /** The baseline, read and, where [write] is set, written; it need not exist. */
    @get:Internal
    abstract val baseline: RegularFileProperty

    /** How a message names the baseline: its path from the repository's root. */
    @get:Input
    abstract val shownBaseline: Property<String>

    /** The Android Gradle plugin the app is built with, from the version catalog. */
    @get:Input
    abstract val agp: Property<String>

    /** The Kotlin the app and the modules are compiled with, from the version catalog. */
    @get:Input
    abstract val kotlin: Property<String>

    /** Whether the figures are recorded as the baseline rather than compared with it. */
    @get:Input
    abstract val write: Property<Boolean>

    /** The Android SDK the command-line tools are looked for in, `ANDROID_HOME`, or unset for the `PATH` alone. */
    @get:Input
    @get:Optional
    abstract val androidHome: Property<String>

    /** The runner of `apkanalyzer`. */
    @get:Inject
    abstract val exec: ExecOperations

    /** Measures the app, then records the figures or holds them to the baseline, failing with every finding. */
    @TaskAction
    fun check() {
        val packages =
            try {
                DexPackages.read(analyze())
            } catch (refused: IllegalArgumentException) {
                throw GradleException(
                    "$NAME: apkanalyzer's output reads as no list of packages, since ${refused.message}.",
                )
            }
        val unnamed =
            try {
                SizeBaseline.unnamed(ReleaseGraph.read(graph.get().asFile.readText()).unnamed())
            } catch (refused: IllegalArgumentException) {
                throw GradleException(
                    "$NAME: the graph the app's build wrote reads as no graph, since ${refused.message}.",
                )
            }
        if (write.get() && unnamed.isNotEmpty()) {
            throw GradleException(
                (
                    listOf("$NAME: nothing recorded, since the SDK brings a library no share names:") +
                        unnamed.map { "  $it" }
                ).joinToString("\n"),
            )
        }
        val followed = scenario.orNull
        val absent = "the contract checkout is absent, so the version of sdk/trail.yaml Trail follows is unknown"
        if (followed == null && (write.get() || ci.get())) {
            throw GradleException(
                "$NAME: $absent, and a baseline is recorded and compared at a version alone: fetch the schemas " +
                    "checkout beside this one, or name one with -P${ContractCheckout.PROPERTY}.",
            )
        }
        val size =
            try {
                val resolved = ResolvedVersions.read(versions.get().asFile.readText())
                AppSize.measure(packages, resolved, followed ?: 0, agp.get(), kotlin.get())
            } catch (refused: IllegalArgumentException) {
                throw GradleException(
                    "$NAME: the versions the app's build wrote give no library the SDK brings its version, " +
                        "since ${refused.message}.",
                )
            }
        val file = baseline.get().asFile
        val shown = shownBaseline.get()
        val verdict =
            if (followed == null) {
                val note = "$absent: the figures are reported and not compared"
                SizeVerdict(emptyList(), listOf(note) + SizeBaseline.describe(size, null))
            } else if (write.get()) {
                file.parentFile.mkdirs()
                file.writeText(SizeBaseline.print(size))
                val note =
                    "recorded $shown, measured against version ${size.scenario} of sdk/trail.yaml with the " +
                        "Android Gradle plugin ${size.agp} and Kotlin ${size.kotlin}, each library the SDK brings at " +
                        "the version the app's build resolved"
                SizeVerdict(emptyList(), listOf(note) + SizeBaseline.describe(size, null))
            } else {
                SizeBaseline.compare(size, file.takeIf { it.isFile }?.readText(), shown)
            }

        val summary =
            "$NAME: the dex code of Trail's release build, examples/trail, ${SizeBaseline.formatBytes(size.dex)}, " +
                "read package by package against $shown"
        val report = (listOf(summary) + verdict.notes.map { "  $it" }).joinToString("\n")
        val findings = unnamed + verdict.findings
        if (findings.isEmpty()) {
            logger.lifecycle(report)
            return
        }
        throw GradleException((listOf(report) + findings.map { "  $it" }).joinToString("\n"))
    }

    /** What `apkanalyzer dex packages` prints of the app's dex code, its classes defined in the APK alone. */
    private fun analyze(): String {
        val tool = apkanalyzer()
        val output = ByteArrayOutputStream()
        val errors = ByteArrayOutputStream()
        val result =
            try {
                exec.exec {
                    commandLine(
                        tool,
                        "dex",
                        "packages",
                        "--defined-only",
                        "--proguard-mappings",
                        mapping.get().asFile.path,
                        apk.get().asFile.path,
                    )
                    standardOutput = output
                    errorOutput = errors
                    isIgnoreExitValue = true
                }
            } catch (failed: Exception) {
                throw GradleException(
                    "$NAME: $tool could not be started (${failed.message}): it ships in the Android SDK's " +
                        "command-line tools mise.toml pins, so run the gate under mise, `mise run size:check`, " +
                        "after `mise install`.",
                    failed,
                )
            }
        if (result.exitValue != 0) {
            throw GradleException("$NAME: $tool failed with exit code ${result.exitValue}: ${errors.toString().trim()}")
        }
        return output.toString(Charsets.UTF_8)
    }

    /**
     * The `apkanalyzer` of the newest command-line tools the Android SDK holds, `latest` before any version, where
     * [androidHome] names one that holds it, and the one on the `PATH` otherwise.
     */
    private fun apkanalyzer(): String {
        val home = androidHome.orNull ?: return TOOL
        val installed =
            File(home, "cmdline-tools")
                .listFiles()
                .orEmpty()
                .filter { File(it, "bin/$TOOL").canExecute() }
        val chosen =
            installed.firstOrNull { it.name == "latest" }
                ?: installed.maxWithOrNull(compareBy<File, List<Int>>(VERSIONS) { version(it.name) })
        return chosen?.let { File(it, "bin/$TOOL").path } ?: TOOL
    }

    /** The task's name and the tool it runs. */
    companion object {
        /** The task's name, which every message opens with. */
        const val NAME: String = "checkAppSize"

        private const val TOOL = "apkanalyzer"

        /** The numbers of a version directory's name, `23.0` as 23 then 0. */
        private fun version(name: String): List<Int> = name.split('.').map { it.toIntOrNull() ?: -1 }

        /** Two versions in order, number by number, a missing number below every other. */
        private val VERSIONS: Comparator<List<Int>> =
            Comparator { a, b ->
                for (index in 0 until maxOf(a.size, b.size)) {
                    val compared = (a.getOrNull(index) ?: -1).compareTo(b.getOrNull(index) ?: -1)
                    if (compared != 0) return@Comparator compared
                }
                0
            }
    }
}
