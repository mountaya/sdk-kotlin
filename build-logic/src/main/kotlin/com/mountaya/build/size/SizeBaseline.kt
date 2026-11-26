package com.mountaya.build.size

import groovy.json.JsonSlurper
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * SizeVerdict is what a comparison with the baseline found: the [findings] that fail the gate, and the [notes] it
 * prints either way.
 */
data class SizeVerdict(
    /** What fails the gate. */
    val findings: List<String>,
    /** What the gate prints either way. */
    val notes: List<String>,
)

/**
 * SizeBaseline is the baseline of the app-size gate, `examples/trail/size/android.json` (SPEC.md section 7): the
 * version of the scenario, `sdk/trail.yaml`'s, the measured app followed, each share the gate holds, the whole dex
 * code, the version each library the SDK brings resolved at, and the Android Gradle plugin and the Kotlin the build was
 * made with, beside the build that measured them.
 */
object SizeBaseline {
    /** The command that records the baseline, which every message of the gate names. */
    const val RECORD: String = "MOUNTAYA_SIZE_BASELINE=write mise run size:check"

    /** The platform the baseline holds the build of. */
    const val PLATFORM: String = "android"

    /** The build the baseline's figures are measured from, as a person would run it. */
    const val BUILD: String = "./gradlew -p examples/trail assembleRelease"

    /** The floor of the tolerance, in bytes: 16 KiB. */
    private const val FLOOR: Long = 16 * 1024L

    /**
     * Returns the growth a share may take on [baseline] bytes before the gate fails: 2% of it or 16 KiB, whichever is
     * larger.
     *
     * R8 run twice over the same classes with one Android Gradle plugin writes the same dex code, so the tolerance absorbs
     * no noise: it absorbs the ordinary growth of a change that adds code the app calls, so such a change does not record
     * the baseline each time, while what the gate exists for is larger, a keep rule or a reflective read that holds a
     * printed surface R8 would shrink. The floor holds a share that is small or 0 at its baseline, where 2% is a few bytes.
     */
    fun tolerance(baseline: Long): Long = maxOf((baseline * 2 / 100.0).roundToLong(), FLOOR)

    /** Returns the baseline holding [size], as the gate writes it, its shares in the order the gate holds them. */
    fun print(size: AppSize): String {
        fun figures(shares: Map<String, Long>): String =
            shares.entries.joinToString(",\n") { (share, bytes) -> "    ${quoted(share)}: $bytes" }
        return buildString {
            appendLine("{")
            appendLine("  \"platform\": ${quoted(PLATFORM)},")
            appendLine("  \"build\": ${quoted(BUILD)},")
            appendLine("  \"scenario\": ${size.scenario},")
            appendLine("  \"agp\": ${quoted(size.agp)},")
            appendLine("  \"kotlin\": ${quoted(size.kotlin)},")
            appendLine("  \"dex\": ${size.dex},")
            appendLine("  \"modules\": {")
            appendLine(figures(size.modules))
            appendLine("  },")
            appendLine("  \"brought\": {")
            appendLine(figures(size.brought))
            appendLine("  },")
            appendLine("  \"versions\": {")
            appendLine(
                size.versions.entries.joinToString(",\n") { (share, version) ->
                    "    ${quoted(share)}: ${quoted(version)}"
                },
            )
            appendLine("  }")
            appendLine("}")
        }
    }

    /** Reads a baseline [print] wrote, failing with an [IllegalArgumentException] that says why where [text] is none. */
    fun read(text: String): AppSize {
        val document =
            try {
                JsonSlurper().parseText(text)
            } catch (error: RuntimeException) {
                throw IllegalArgumentException("it is no JSON: ${error.message}", error)
            }
        require(document is Map<*, *>) { "it holds no object" }
        val platform = document["platform"]
        require(platform == PLATFORM) { "it records ${platform ?: "no platform"}, not $PLATFORM" }
        val scenario = document["scenario"]
        require(scenario is Int && scenario >= 1) { "it records no version of the scenario it measured, `scenario`" }
        val agp = document["agp"]
        val kotlin = document["kotlin"]
        require(agp is String && kotlin is String) { "it names no Android Gradle plugin and Kotlin" }
        return AppSize(
            scenario = scenario,
            agp = agp,
            kotlin = kotlin,
            dex = count(document["dex"], "dex"),
            modules = figures(document["modules"], "modules"),
            brought = figures(document["brought"], "brought"),
            versions = versions(document["versions"]),
        )
    }

    /**
     * Holds [measured] to [baseline], the text of the baseline at [path], or null where there is none: a share that grows
     * past its [tolerance] is a finding, and one that shrinks past it a note, since a smaller app is no defect and recording
     * it is the person's choice. A baseline measured against another version of the scenario is reported and not
     * compared, since the scenario decides what the app does and so what R8 keeps; so is one measured with another
     * Android Gradle plugin or another Kotlin, since R8 and the compiler shape the dex code as much as the SDK does. So is
     * the share of a library the SDK brings that resolved at another version than the one the baseline records it at,
     * since that library's own release shapes its share as much as the SDK does, and a release a range admits,
     * MapLibre's, lands on a commit that changed nothing; the SDK's own shares are compared whatever its libraries
     * resolved at. The notes end on the SDK's modules together and the whole dex code, neither compared.
     */
    fun compare(
        measured: AppSize,
        baseline: String?,
        path: String,
    ): SizeVerdict {
        if (baseline == null) return SizeVerdict(listOf("$path: no baseline: run $RECORD"), describe(measured, null))
        val recorded =
            try {
                read(baseline)
            } catch (refused: IllegalArgumentException) {
                return SizeVerdict(
                    listOf("$path is no size baseline, since ${refused.message}: run $RECORD"),
                    describe(measured, null),
                )
            }
        if (recorded.scenario != measured.scenario) {
            val note =
                "Trail follows version ${measured.scenario} of sdk/trail.yaml, and the baseline was measured " +
                    "against version ${recorded.scenario}, so the figures are reported and not compared: the " +
                    "scenario decides what the app does and so what R8 keeps, and $RECORD records them for this version"
            return SizeVerdict(emptyList(), listOf(note) + describe(measured, recorded))
        }
        if (recorded.agp != measured.agp || recorded.kotlin != measured.kotlin) {
            val note =
                "built with the Android Gradle plugin ${measured.agp} and Kotlin ${measured.kotlin}, and the " +
                    "baseline with ${recorded.agp} and ${recorded.kotlin}, so the figures are reported and not " +
                    "compared: R8 and the compiler shape the dex code as much as the SDK does, and $RECORD records " +
                    "them for this toolchain"
            return SizeVerdict(emptyList(), listOf(note) + describe(measured, recorded))
        }
        val findings = mutableListOf<String>()
        val notes = mutableListOf<String>()
        for ((share, bytes) in measured.shares) {
            val was = recorded.shares[share]
            if (was == null) {
                findings += "$path records no `$share` share, which the gate now holds: run $RECORD"
                continue
            }
            val version = measured.versions[share]
            if (version != null) {
                val recordedAt = recorded.versions[share]
                if (recordedAt == null) {
                    findings += "$path records no version of `$share`, which the gate now holds: run $RECORD"
                    continue
                }
                if (recordedAt != version) {
                    notes += "${label(share)} resolved at $version where the baseline records $recordedAt, so its " +
                        "${formatBytes(bytes)} is reported and not compared with the baseline's ${formatBytes(was)}: " +
                        "the library's own release shapes its share as much as the SDK does, and $RECORD records it " +
                        "at $version"
                    continue
                }
            }
            val tolerance = tolerance(was)
            val change = bytes - was
            val moved =
                "${formatBytes(was)} to ${formatBytes(bytes)}, ${signed(change)}" +
                    if (was == 0L) "" else " (${signedPercent(change, was)})"
            when {
                change > tolerance -> {
                    findings += "$path: ${label(share)} grew from $moved, past the tolerance of " +
                        "${formatBytes(
                            tolerance,
                        )}: find what a keep rule or a reflective read holds that R8 would shrink, " +
                        "and after a deliberate change run $RECORD"
                }

                -change > tolerance -> {
                    notes += "${label(share)} shrank from $moved: $RECORD records the smaller figure"
                }

                else -> {
                    notes += "${label(share)} ${formatBytes(bytes)}, ${signed(change)} on the baseline, within " +
                        formatBytes(tolerance)
                }
            }
        }
        notes += total(measured)
        return SizeVerdict(findings, notes)
    }

    /**
     * Returns the findings that fail the gate for [unnamed], each library an SDK module brings that no share names,
     * whatever the baseline says and whether or not it is being recorded: a library no share names is code the app pays
     * for that the gate holds to nothing.
     */
    fun unnamed(unnamed: List<ReleaseGraph.Unnamed>): List<String> =
        unnamed.map { library ->
            "`${library.library}` reaches the app through `${library.through}`, and no share names it: name it in " +
                "AppSize, its package in BROUGHT with the module its version is read at and its modules in NAMES, " +
                "then run $RECORD"
        }

    /**
     * Returns one line per share of [size], a library's at the version it resolved at, with the figure [baseline]
     * records beside it where one is given.
     */
    fun describe(
        size: AppSize,
        baseline: AppSize?,
    ): List<String> =
        size.shares.map { (share, bytes) ->
            val was = baseline?.shares?.get(share)
            val beside =
                if (baseline == null || was == null) "" else ", the baseline ${formatBytes(was)}${at(baseline, share)}"
            "${label(share)} ${formatBytes(bytes)}${at(size, share)}$beside"
        } + total(size)

    /** The SDK's modules together and the whole dex code, the report's last line, which the gate never compares. */
    private fun total(size: AppSize): String =
        "the SDK's modules together ${formatBytes(size.sdk)}, of the ${formatBytes(size.dex)} of the app's dex code"

    /** Returns [bytes] as a person reads them: `512 B`, `12.3 KiB`, `1.18 MiB`. */
    fun formatBytes(bytes: Long): String {
        val kib = 1024.0
        val mib = kib * 1024
        return when {
            abs(bytes) < kib -> "$bytes B"
            abs(bytes) < mib -> String.format(Locale.ROOT, "%.1f KiB", bytes / kib)
            else -> String.format(Locale.ROOT, "%.2f MiB", bytes / mib)
        }
    }

    /** How a message names [share]: a module's package as the SDK's, a library as one the SDK brings. */
    private fun label(share: String): String =
        if (share in AppSize.MODULES) "`$share`'s share" else "`$share`'s share, which the SDK brings,"

    /** The version [size] records [share]'s library at, as a message writes it after its figure, or nothing for a module. */
    private fun at(
        size: AppSize,
        share: String,
    ): String = size.versions[share]?.let { " at $it" }.orEmpty()

    private fun signed(change: Long): String = if (change < 0) "-${formatBytes(-change)}" else "+${formatBytes(change)}"

    private fun signedPercent(
        change: Long,
        baseline: Long,
    ): String = (if (change < 0) "" else "+") + String.format(Locale.ROOT, "%.1f%%", change * 100.0 / baseline)

    private fun quoted(text: String): String = "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

    private fun count(
        value: Any?,
        name: String,
    ): Long {
        require(value is Number && value.toLong() >= 0) { "`$name` holds no count of bytes" }
        return value.toLong()
    }

    private fun figures(
        value: Any?,
        name: String,
    ): Map<String, Long> {
        require(value is Map<*, *>) { "it holds no `$name`" }
        return value.entries.associate { (share, bytes) -> share.toString() to count(bytes, "$name.$share") }
    }

    private fun versions(value: Any?): Map<String, String> {
        require(value is Map<*, *>) { "it holds no `versions`" }
        return value.entries.associate { (share, version) ->
            require(version is String && version.isNotBlank()) { "`versions.$share` holds no version" }
            share.toString() to version
        }
    }
}
